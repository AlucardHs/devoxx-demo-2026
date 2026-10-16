package com.example.conference;

import com.example.conference.chat.ConferenceTools;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrail;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.repository.GenericRepository;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.serde.annotation.Serdeable;

import static com.tngtech.archunit.core.domain.JavaCall.Predicates.target;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.nameMatching;
import static com.tngtech.archunit.core.domain.properties.HasOwner.Predicates.With.owner;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.example.conference", importOptions = {ImportOption.DoNotIncludeTests.class, ArchitectureTest.IgnoreMicronautGenerated.class})
class ArchitectureTest {

    /** Micronaut generates bean definitions like {@code $TalkController$Definition} at compile time. */
    static class IgnoreMicronautGenerated implements ImportOption {
        @Override
        public boolean includes(Location location) {
            return !location.contains("/$");
        }
    }

    // --- Structure ---------------------------------------------------------

    @ArchTest
    static final ArchRule layers = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Controller").definedBy("..controller..")
            .layer("Chat").definedBy("..chat..")
            .layer("Repository").definedBy("..repository..")
            .layer("Model").definedBy("..model..")
            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Chat").mayNotBeAccessedByAnyLayer()
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Controller", "Chat")
            .because("the web layer and the agent both read through repositories, never through each other");

    @ArchTest
    static final ArchRule noPackageCycles = slices()
            .matching("com.example.conference.(*)..")
            .should().beFreeOfCycles();

    // --- The programme is read-only ----------------------------------------

    @ArchTest
    static final ArchRule noWritesToTheDatabase = noClasses()
            .should().callMethodWhere(target(owner(assignableTo(GenericRepository.class)))
                    .and(target(nameMatching("(save|update|delete|insert|persist).*"))))
            .because("the conference programme is read-only: neither the API nor the agent may change data");

    @ArchTest
    static final ArchRule scheduleApiOnlyServesGets = methods()
            .that().areDeclaredInClassesThat().resideInAPackage("..controller..")
            .and().arePublic()
            .should().beAnnotatedWith(Get.class)
            .because("the schedule API is read-only");

    // --- The agent ---------------------------------------------------------

    @ArchTest
    static final ArchRule toolsLiveInOnePlace = methods()
            .that().areAnnotatedWith(Tool.class)
            .should().beDeclaredIn(ConferenceTools.class)
            .because("every capability the agent has should be reviewable in a single class");

    @ArchTest
    static final ArchRule guardrailsAreOneClassEach = classes()
            .that().implement(InputGuardrail.class).or().implement(OutputGuardrail.class)
            .should().resideInAPackage("..chat.guardrail..")
            .andShould().haveSimpleNameEndingWith("Guardrail");

    // --- Conventions -------------------------------------------------------

    @ArchTest
    static final ArchRule entitiesAreSerdeableRecords = classes()
            .that().areAnnotatedWith(MappedEntity.class)
            .should().resideInAPackage("..model..")
            .andShould().beAnnotatedWith(Serdeable.class)
            .andShould().beRecords();

    @ArchTest
    static final ArchRule controllersAreNamedController = classes()
            .that().areAnnotatedWith(Controller.class)
            .should().haveSimpleNameEndingWith("Controller");

    @ArchTest
    static final ArchRule noJackson = noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.fasterxml.jackson..")
            .because("serialisation is Micronaut Serde (@Serdeable)");

    @ArchTest
    static final ArchRule noJavaUtilDate = noClasses()
            .should().dependOnClassesThat().haveFullyQualifiedName("java.util.Date")
            .because("conference times are local times without a zone: use java.time.LocalDateTime");

    @ArchTest
    static final ArchRule noFieldInjection = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

    @ArchTest
    static final ArchRule noSystemOut = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
            .because("use an SLF4J logger");
}
