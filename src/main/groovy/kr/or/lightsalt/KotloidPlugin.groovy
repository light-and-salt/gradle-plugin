package kr.or.lightsalt

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownProjectException
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies

class KotloidPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.extensions.create("kotloid", KotloidPluginExtension)
		project.plugins.apply "kotlin-android"
		project.plugins.apply "kotlin-kapt"
		project.plugins.apply "kotlin-android-extensions"
		project.afterEvaluate {
			project.android.sourceSets.all { sourceSet ->
				if (!sourceSet.name.startsWith("test")) {
					sourceSet.kotlin.setSrcDirs([])
				}
			}
		}
		project.android {
			sourceSets {
				main.java.srcDirs += 'src/main/kotlin'
				test.java.srcDirs += 'src/test/kotlin'
				androidTest.java.srcDirs += 'src/androidTest/kotlin'
			}
		}
		try {
			def kotloid = project.rootProject.project(':kotloid')
			project.dependencies.add("implementation", kotloid)
		} catch (UnknownProjectException ignored) {
			project.dependencies.add("implementation", "kr.or.lightsalt:kotloid:$project.KOTLIB_VERSION") {
				changing = true
			}
		}
		def testImplementation = project.configurations.getByName("testImplementation").dependencies
		project.gradle.addListener(new DependencyResolutionListener() {
			@Override
			void beforeResolve(ResolvableDependencies resolvableDependencies) {
				def kotlin_ver = project.kotloid.kotlinVersion ?: project.KOTLIN_VERSION
				project.dependencies.with {
					testImplementation.add(create("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_ver"))
				}
				project.gradle.removeListener(this)
			}

			@Override
			void afterResolve(ResolvableDependencies resolvableDependencies) {

			}
		})
	}
}
