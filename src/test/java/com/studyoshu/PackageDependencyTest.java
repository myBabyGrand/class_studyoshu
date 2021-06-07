package com.studyoshu;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packagesOf =  App.class)
public class PackageDependencyTest {

    private static final String STUDY = "..modules.study..";
    private static final String EVENT = "..modules.event..";
    private static final String ACCOUNT = "..modules.account..";
    private static final String TAG = "..modules.tag..";
    private static final String ZONE = "..modules.zone..";

    @ArchTest
    ArchRule modulesPackageRule = classes().that().resideInAPackage("com.studyoshu.modules..")
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage("com.studyoshu.modules..");

    @ArchTest
    ArchRule studyPackageRule = classes().that().resideInAPackage(STUDY)
    .should().onlyBeAccessed().byClassesThat().resideInAnyPackage(STUDY, EVENT);// STUDY <- STUDY, EVENT

    @ArchTest
    ArchRule eventPackageRule = classes().that().resideInAPackage(EVENT)
            .should().accessClassesThat().resideInAnyPackage(STUDY, ACCOUNT, EVENT);//EVENT -> STUDY, ACCOUNT, EVENT

    @ArchTest
    ArchRule accountPackageRule = classes().that().resideInAPackage(ACCOUNT)
            .should().accessClassesThat().resideInAnyPackage(TAG, ZONE, ACCOUNT);

    @ArchTest
    ArchRule cycleCheck = slices().matching("com.studyoshu.modules.(*)..")
            .should().beFreeOfCycles();
}