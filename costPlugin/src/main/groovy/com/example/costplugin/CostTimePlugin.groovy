package com.example.costplugin

import com.example.asm.CostClassVisitor
import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.example.extension.CostExtension
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter


class CostTimePlugin extends Transform implements Plugin<Project> {
    def costDebug

    @Override
    void apply(Project project) {

        project.extensions.create("costTime", CostExtension)

        def android = project.extensions.getByName("android")
        android.registerTransform(this)

        project.afterEvaluate {

            def extension = project.extensions.findByName("costTime") as CostExtension
            costDebug = extension.debug;

        }

    }

    /**
     * Transform 标识名
     * 比如我在 app module 下依赖了这个 Plugin
     * 那么在 app/build/intermediates/transforms/
     * 下，就能看到我们的自定义 DemoTransform
     */
    @Override
    String getName() {
        return "DemoTransform"
    }

    /**
     * 设置文件输入类型
     * 类型在 TransformManager 下有定义
     * 这里我们获取 class 文件类型
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 设置文件所属域
     * 同样在 TransformManager 下有定义
     * 这里指定为当前工程
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    /**
     * 是否支持增量编译
     */
    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider,
                   boolean isIncremental) throws IOException, TransformException, InterruptedException {
        println '//===============asm visit start===============//'
        def startTime = System.currentTimeMillis()
        inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->

                if (directoryInput.file.isDirectory()) {
                    directoryInput.file.eachFileRecurse { File file ->
                        def name = file.name
//                        过滤R、BuildConfig 文件
                        if (name.endsWith(".class") && !name.startsWith("R\$") &&
                                !"R.class".equals(name) && !"BuildConfig.class".equals(name)) {

                            println name + ' is changing...'

                            ClassReader cr = new ClassReader(file.bytes)
                            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
                            ClassVisitor cv = new CostClassVisitor(cw)

                            cr.accept(cv, ClassReader.EXPAND_FRAMES)

                            if (costDebug) {

                                byte[] code = cw.toByteArray()

                                println file.parentFile.absolutePath + File.separator + name

                                FileOutputStream fos = new FileOutputStream(
                                        file.parentFile.absolutePath + File.separator + name)
                                fos.write(code)
                                fos.close()
                            }
                        }
                    }
                }


                //处理完输入文件之后，要把输出给下一个任务
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
            //对类型为jar文件的input进行遍历
            input.jarInputs.each { JarInput jarInput ->
                //jar文件一般是第三方依赖库jar文件

                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //生成输出路径
                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                //将输入内容复制到输出
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
        def cost = (System.currentTimeMillis() - startTime) / 1000
        println "plugin cost $cost secs"
        println '//===============asm visit end===============//'
    }

}