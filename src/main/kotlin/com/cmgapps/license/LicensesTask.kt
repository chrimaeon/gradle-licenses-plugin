/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license

import com.cmgapps.license.model.MavenCoordinates
import com.cmgapps.license.model.PomLibrary
import com.cmgapps.license.model.PomLicense
import com.cmgapps.license.reporter.CsvReport
import com.cmgapps.license.reporter.CustomReport
import com.cmgapps.license.reporter.HtmlReport
import com.cmgapps.license.reporter.JsonReport
import com.cmgapps.license.reporter.LicensesSingleFileReport
import com.cmgapps.license.reporter.MarkdownReport
import com.cmgapps.license.reporter.ReportType
import com.cmgapps.license.reporter.TextReport
import com.cmgapps.license.reporter.XmlReport
import com.cmgapps.license.repository.SpdxIdRepository
import com.cmgapps.license.repository.internal.SpdxIdRepositoryImpl
import groovy.lang.Closure
import org.apache.maven.model.Dependency
import org.apache.maven.model.Model
import org.apache.maven.model.Parent
import org.apache.maven.model.Repository
import org.apache.maven.model.building.DefaultModelBuilderFactory
import org.apache.maven.model.building.DefaultModelBuildingRequest
import org.apache.maven.model.building.FileModelSource
import org.apache.maven.model.building.ModelBuildingRequest
import org.apache.maven.model.building.ModelSource2
import org.apache.maven.model.resolution.ModelResolver
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.artifacts.result.ResolvedVariantResult
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Category.ENFORCED_PLATFORM
import org.gradle.api.attributes.Category.REGULAR_PLATFORM
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ReportContainer
import org.gradle.api.reporting.Reporting
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.function.IntFunction
import javax.inject.Inject

interface LicenseReportContainer : ReportContainer<LicensesSingleFileReport> {
    @get:Internal
    val plainText: TextReport

    @get:Internal
    val json: JsonReport

    @get:Internal
    val html: HtmlReport

    @get:Internal
    val xml: XmlReport

    @get:Internal
    val markdown: MarkdownReport

    @get:Internal
    val csv: CsvReport

    @get:Internal
    val custom: CustomReport
}

internal abstract class LicenseReportContainerImpl
    @Inject
    constructor(
        private val objects: ObjectFactory,
        layout: ProjectLayout,
        task: Task,
    ) : NamedDomainObjectSet<LicensesSingleFileReport> by objects.namedDomainObjectSet(LicensesSingleFileReport::class.java),
        LicenseReportContainer {
        private val spdxIdRepository: SpdxIdRepository = SpdxIdRepositoryImpl(layout)

        init {
            addReporter(TextReport::class.java, layout, task, spdxIdRepository)
            addReporter(JsonReport::class.java, layout, task, spdxIdRepository)
            addReporter(HtmlReport::class.java, layout, task, task.logger, objects, spdxIdRepository)
            addReporter(XmlReport::class.java, layout, task, spdxIdRepository)
            addReporter(MarkdownReport::class.java, layout, task, task.logger, spdxIdRepository)
            addReporter(CsvReport::class.java, layout, task, spdxIdRepository)
            addReporter(CustomReport::class.java, layout, task, objects, spdxIdRepository)
        }

        override fun getEnabled(): NamedDomainObjectSet<LicensesSingleFileReport> = enabledReports

        @Override
        override fun getEnabledReports(): MutableMap<String, LicensesSingleFileReport> = enabledReports.asMap

        private val enabledReports: NamedDomainObjectSet<LicensesSingleFileReport>
            get() = matching { element -> element.required.get() }

        override fun configure(cl: Closure<*>?): ReportContainer<LicensesSingleFileReport> {
            cl?.call(this)
            return this
        }

        private fun addReporter(
            clazz: Class<out LicensesSingleFileReport>,
            vararg params: Any,
        ) {
            add(objects.newInstance(clazz, *params))
        }

        override val plainText: TextReport
            get() = withType(TextReport::class.java).getByName(ReportType.TEXT.name)

        override val json: JsonReport
            get() = withType(JsonReport::class.java).getByName(ReportType.JSON.name)

        override val html: HtmlReport
            get() = withType(HtmlReport::class.java).getByName(ReportType.HTML.name)

        override val xml: XmlReport
            get() = withType(XmlReport::class.java).getByName(ReportType.XML.name)

        override val csv: CsvReport
            get() = withType(CsvReport::class.java).getByName(ReportType.CSV.name)

        override val markdown: MarkdownReport
            get() = withType(MarkdownReport::class.java).getByName(ReportType.MARKDOWN.name)

        override val custom: CustomReport
            get() = withType(CustomReport::class.java).getByName(ReportType.CUSTOM.name)

        @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
        override fun <T> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?>? = super<LicenseReportContainer>.toArray(generator)
    }

@CacheableTask
abstract class LicensesTask
    @Inject
    constructor(
        objects: ObjectFactory,
    ) : DefaultTask(),
        Reporting<LicenseReportContainer> {
        private val reports: LicenseReportContainer =
            objects.newInstance(LicenseReportContainerImpl::class.java, this)

        @get:Input
        internal abstract val coordinatesWithPomLibrary: MapProperty<MavenCoordinates, PomLibrary>

        @get:OutputFiles
        val outputFiles: Map<String, RegularFileProperty>
            get() = reports.enabledReports.mapValues { it.value.outputLocation }

        @Internal
        override fun getReports(): LicenseReportContainer = reports

        override fun reports(closure: Closure<*>): LicenseReportContainer =
            reports.apply {
                @Suppress("UNCHECKED_CAST")
                (closure as Closure<LicenseReportContainer>).delegate = this
                closure.resolveStrategy = Closure.DELEGATE_FIRST
                closure.call(this)
            }

        override fun reports(configureAction: Action<in LicenseReportContainer>): LicenseReportContainer =
            reports.apply {
                configureAction.execute(this)
            }

        fun configurationToCheck(configuration: Configuration) {
            loadDependenciesFromConfiguration(configuration.incoming.resolutionResult.rootComponent)
        }

        fun configurationToCheck(configuration: Provider<Configuration>) {
            loadDependenciesFromConfiguration(
                configuration.flatMap { it.incoming.resolutionResult.rootComponent },
            )
        }

        private fun loadDependenciesFromConfiguration(root: Provider<ResolvedComponentResult>) {
            val dependencies = project.dependencies
            val configurations = project.configurations
            val pomInfos: Provider<Map<MavenCoordinates, PomLibrary>> =
                root.map { root ->
                    val directDependencies = loadMavenCoordinates(logger, root)
                    val directPomFiles =
                        directDependencies.fetchPomFiles(root.variants, dependencies, configurations)
                    directPomFiles.getPomInfo(root.variants, dependencies, configurations)
                }

            this.coordinatesWithPomLibrary.set(pomInfos)
        }

        @TaskAction
        fun licensesReport() {
            createReport(coordinatesWithPomLibrary.get().toSortedMap())
        }

        private fun Iterable<MavenCoordinatesWithPomFile>.getPomInfo(
            variants: List<ResolvedVariantResult>,
            dependencies: DependencyHandler,
            configurations: ConfigurationContainer,
        ): Map<MavenCoordinates, PomLibrary> {
            val builder = DefaultModelBuilderFactory().newInstance()
            val resolver =
                object : ModelResolver {
                    fun resolve(dependencyCoordinates: MavenCoordinates): FileModelSource {
                        val pomFile =
                            setOf(dependencyCoordinates)
                                .fetchPomFiles(variants, dependencies, configurations)
                                .single()
                                .pomFile
                        return FileModelSource(pomFile)
                    }

                    override fun resolveModel(
                        groupId: String,
                        artifactId: String,
                        version: String,
                    ): ModelSource2 = resolve(MavenCoordinates(groupId, artifactId, version))

                    override fun resolveModel(parent: Parent): ModelSource2 =
                        resolve(MavenCoordinates(parent.groupId, parent.artifactId, parent.version))

                    override fun resolveModel(dependency: Dependency): ModelSource2 =
                        resolve(
                            MavenCoordinates(dependency.groupId, dependency.artifactId, dependency.version),
                        )

                    override fun addRepository(repository: Repository) {
                        // NO-OP
                    }

                    override fun addRepository(
                        repository: Repository,
                        replace: Boolean,
                    ) {
                        // NO-OP
                    }

                    override fun newCopy(): ModelResolver = this
                }

            return associate { (coordinates, file) ->
                val req =
                    DefaultModelBuildingRequest().apply {
                        isProcessPlugins = false
                        pomFile = file
                        isTwoPhaseBuilding = true
                        modelResolver = resolver
                        validationLevel = ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL
                    }
                val result = builder.build(req)
                coordinates to loadPomInfo(result.effectiveModel) { modelId -> result.getRawModel(modelId) }
            }
        }

        private fun Set<MavenCoordinates>.fetchPomFiles(
            variants: List<ResolvedVariantResult>,
            dependencies: DependencyHandler,
            configurations: ConfigurationContainer,
        ): List<MavenCoordinatesWithPomFile> {
            val pomDependencies = map { dependencies.create(it.pomCoordinate()) }.toTypedArray()

            val withVariants =
                configurations
                    .detachedConfiguration(*pomDependencies)
                    .apply {
                        for (variant in variants) {
                            attributes {
                                val variantAttrs = variant.attributes
                                for (attrs in variantAttrs.keySet()) {
                                    @Suppress("UNCHECKED_CAST")
                                    it.attribute(attrs as Attribute<Any>, variantAttrs.getAttribute(attrs)!!)
                                }
                            }
                        }
                    }.artifacts()

            val withoutVariants = configurations.detachedConfiguration(*pomDependencies).artifacts()

            return (withVariants + withoutVariants)
                .map {
                    // Cast is safe because all resolved artifacts are pom files.
                    val coordinates =
                        (it.id.componentIdentifier as ModuleComponentIdentifier).toMavenCoordinates()
                    MavenCoordinatesWithPomFile(coordinates, it.file)
                }.distinctBy { it.dependencyCoordinates }
        }

        private fun Configuration.artifacts() =
            resolvedConfiguration.lenientConfiguration.allModuleDependencies.flatMap {
                it.allModuleArtifacts
            }

        private fun createReport(libraries: Map<MavenCoordinates, PomLibrary>) {
            if (libraries.isEmpty()) {
                return
            }

            reports.filter { it.required.get() }.forEach { report ->
                report.libraries = libraries
                report.write()
            }
        }

        private fun LicensesSingleFileReport.write() {
            with(this.outputLocation.get().asFile) {
                parentFile.mkdirs()
                outputStream().use {
                    writeLicenses(it)
                    logger.info("${this.name} report saved to $absolutePath")
                }
            }
        }
    }

private fun ModuleComponentIdentifier.toMavenCoordinates(): MavenCoordinates =
    MavenCoordinates(
        groupId = group,
        artifactId = module,
        version = version,
    )

private data class MavenCoordinatesWithPomFile(
    val dependencyCoordinates: MavenCoordinates,
    val pomFile: File,
)

private fun loadMavenCoordinates(
    logger: Logger,
    root: ResolvedComponentResult,
): Set<MavenCoordinates> {
    val coordinates = mutableSetOf<MavenCoordinates>()

    loadMavenCoordinates(
        logger,
        root,
        coordinates,
        mutableSetOf(),
        depth = 1,
    )

    return coordinates
}

private fun loadMavenCoordinates(
    logger: Logger,
    root: ResolvedComponentResult,
    destination: MutableSet<MavenCoordinates>,
    seen: MutableSet<ComponentIdentifier>,
    depth: Int,
) {
    val id = root.id

    when {
        id is ProjectComponentIdentifier -> {
            logDependencyInfo(logger, depth, id, " ignoring because project dependency")
            for (dependency in root.dependencies) {
                if (dependency is ResolvedDependencyResult) {
                    val selected = dependency.selected
                    if (seen.add(selected.id)) {
                        loadMavenCoordinates(
                            logger,
                            selected,
                            destination,
                            seen,
                            depth + 1,
                        )
                    }
                }
            }
        }

        root.isPlatform() -> {
            // Platform (POM) dependency, do nothing.
            logDependencyInfo(logger, depth, id, " ignoring because platform dependency")
        }

        id is ModuleComponentIdentifier -> {
            var ignoreSuffix: String? = null
            if (id.group == "" && id.version == "") {
                // Assuming flat-dir repository dependency, do nothing.
                ignoreSuffix = " ignoring because flat-dir repository artifact has no metadata"
            } else {
                destination += id.toMavenCoordinates()
            }
            logDependencyInfo(logger, depth, id, ignoreSuffix)
        }

        else -> {
            error("Unknown dependency ${id::class.java}: $id")
        }
    }
}

private fun logDependencyInfo(
    logger: Logger,
    depth: Int,
    id: ComponentIdentifier,
    ignoreSuffix: String? = null,
) {
    if (logger.isInfoEnabled) {
        logger.info(
            buildString {
                repeat(depth) { append("  ") }
                append(id)
                if (ignoreSuffix != null) {
                    append(ignoreSuffix)
                }
            },
        )
    }
}

private fun ResolvedComponentResult.isPlatform(): Boolean {
    val singleVariant = variants.singleOrNull() ?: return false
    // https://github.com/gradle/gradle/issues/8854
    val stringAttribute = Attribute.of(CATEGORY_ATTRIBUTE.name, String::class.java)
    val category = singleVariant.attributes.getAttribute(stringAttribute) ?: return false
    return when (category) {
        ENFORCED_PLATFORM,
        REGULAR_PLATFORM,
        -> true

        else -> false
    }
}

internal fun loadPomInfo(
    pom: Model,
    getRawModel: (String) -> Model?,
): PomLibrary {
    val parentRawModel =
        pom.parent?.let { getRawModel("${it.groupId}:${it.artifactId}:${it.version}") }

    return PomLibrary(
        name = pom.name ?: parentRawModel?.name,
        licenses =
            (pom.licenses.takeUnless { it.isEmpty() } ?: parentRawModel?.licenses)?.mapTo(
                mutableSetOf(),
            ) {
                PomLicense(it.name, it.url)
            } ?: emptySet(),
        description = pom.description,
    )
}
