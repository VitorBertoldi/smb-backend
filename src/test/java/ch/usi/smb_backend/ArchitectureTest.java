package ch.usi.smb_backend;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

/**
 * The dependency rules of docs/domain-model.md (sections 1 and 5), checked on every build.
 * Empty packages are allowed, so the rules hold from the first day.
 */
class ArchitectureTest {

	private static final String ROOT = "ch.usi.smb_backend";
	private static final String MODEL = ROOT + ".model";

	private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
			.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
			.importPackages(ROOT);

	@Test
	void modelUsesNoFrameworks() {
		noClasses().that().resideInAPackage(MODEL + "..")
				.should().dependOnClassesThat().resideInAnyPackage(
						"org.springframework..",
						"jakarta.persistence..",
						"org.hibernate..",
						"java.sql..",
						"javax.sql..",
						"tools.jackson..",
						"com.fasterxml.jackson..")
				.because("the model must be testable with plain JUnit, without Spring or a database")
				.allowEmptyShould(true)
				.check(PRODUCTION_CLASSES);
	}

	@Test
	void layersDependInwards() {
		layeredArchitecture().consideringOnlyDependenciesInLayers()
				.withOptionalLayers(true)
				.layer("Controller").definedBy(ROOT + ".controller..")
				.layer("Service").definedBy(ROOT + ".service..")
				.layer("Repository").definedBy(ROOT + ".repository..")
				.layer("Model").definedBy(MODEL + "..")
				.whereLayer("Controller").mayNotBeAccessedByAnyLayer()
				.whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
				.whereLayer("Repository").mayNotBeAccessedByAnyLayer()
				.whereLayer("Model").mayOnlyBeAccessedByLayers("Controller", "Service", "Repository")
				.check(PRODUCTION_CLASSES);
	}

	@Test
	void modelPackagesDependDownwards() {
		layeredArchitecture().consideringOnlyDependenciesInLayers()
				.withOptionalLayers(true)
				.layer("Port").definedBy(MODEL + ".port..")
				.layer("Vehicle").definedBy(MODEL + ".vehicle..")
				.layer("Statistics").definedBy(MODEL + ".statistics..")
				.layer("Timetable").definedBy(MODEL + ".timetable..")
				.layer("Network").definedBy(MODEL + ".network..")
				.layer("Geo").definedBy(MODEL + ".geo..")
				.layer("Time").definedBy(MODEL + ".time..")
				.whereLayer("Port").mayNotBeAccessedByAnyLayer()
				.whereLayer("Vehicle").mayNotBeAccessedByAnyLayer()
				.whereLayer("Statistics").mayOnlyBeAccessedByLayers("Port")
				.whereLayer("Timetable").mayOnlyBeAccessedByLayers("Port", "Vehicle")
				.whereLayer("Network")
				.mayOnlyBeAccessedByLayers("Port", "Vehicle", "Statistics", "Timetable")
				.whereLayer("Geo")
				.mayOnlyBeAccessedByLayers("Port", "Vehicle", "Statistics", "Timetable", "Network")
				.whereLayer("Time")
				.mayOnlyBeAccessedByLayers("Port", "Vehicle", "Statistics", "Timetable", "Network")
				.check(PRODUCTION_CLASSES);
	}
}
