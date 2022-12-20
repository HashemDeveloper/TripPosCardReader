package com.limosys.test.tripostestapp.utils

import android.util.Log
import com.vantiv.triposmobilesdk.EmvTag
import com.vantiv.triposmobilesdk.TlvCollection
import com.vantiv.triposmobilesdk.utilities.BigDecimalUtility
import com.vantiv.triposmobilesdk.utilities.ByteArrayUtility
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.math.BigDecimal
import java.util.*

object ReflectionUtils {

    fun getDeclaredFieldsForObject(obj: Any): Array<Field> {
        val filteredFields = ArrayList<Field>()
        var theClass: Class<*>? = obj.javaClass
        while (theClass != null && theClass.getPackage()?.name?.startsWith("com.vantiv.triposmobilesdk") == true) {
            val fields = theClass.declaredFields
            for (field in fields) {
                if (!field.name.equals(
                        "\$change",
                        ignoreCase = true
                    ) && !field.name.equals("serialversionuid", ignoreCase = true)
                ) {
                    filteredFields.add(field)
                }
            }
            theClass = theClass.superclass
        }
        return filteredFields.toTypedArray()
    }

    @Throws(IllegalAccessException::class)
    fun recursiveToString(obj: Any?): String {
        val newLine = "\n\n"
        val tab = "\t\t\t\t"
        if (obj == null) {
            return "null$newLine"
        }

        // Base case
        if (obj.javaClass.isPrimitive
            || obj.javaClass.isAssignableFrom(String::class.java)
            || obj.javaClass.isEnum
        ) {
            return obj.toString() + newLine
        }
        if (obj.javaClass == BigDecimal::class.java) {
            // currently, the only place we use BigDecimal's are for amounts
            return BigDecimalUtility.formatAsCurrency(obj as BigDecimal?) + newLine
        }
        if (obj.javaClass == TlvCollection::class.java) {
            val tlvCollection = obj as TlvCollection
            val tagResult = StringBuilder(obj.javaClass.simpleName + ": ..." + newLine)
            val entries = tlvCollection.entries
            for (value in entries) {
                val tag: MutableMap.MutableEntry<EmvTag, ByteArray> = value
                tagResult.append(String.format("%02X: %s", tag.key.tagValue, ByteArrayUtility.byteArrayToHexString(tag.value))).append(newLine);
            }
            return tagResult.toString()
        }
        if (obj.javaClass == ArrayList::class.java) {
            val arrayList = obj as ArrayList<*>
            val stringBuilder = StringBuilder(obj.javaClass.simpleName + ": ..." + newLine)
            for (item in arrayList) {
                stringBuilder.append(recursiveToString(item))
            }
            return stringBuilder.toString()
        }
        if (isAndroidType(obj.javaClass) || isWrapperType(obj.javaClass)) {
            return obj.toString() + newLine
        }
        val result = StringBuilder(obj.javaClass.simpleName + ": ..." + newLine)

        // Recursive step
        val methods = obj.javaClass.methods
        for (method in methods) {
            if ((method.name.startsWith("get")
                        || method.name.startsWith("is")
                        || method.name.startsWith("was"))
                && !method.name.startsWith("getType")
                && method.declaringClass != Any::class.java
            ) {
                try {
                    val resultObj = method.invoke(obj)
                    if (resultObj is String) {
                        result.append(tab).append(method.name).append(": ").append(resultObj)
                            .append(newLine) //.append(": ").append(recursiveToString(field.get(obj)));
                    } else {
                        result.append(tab).append(method.name).append(": ")
                            .append(recursiveToString(resultObj))
                    }
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                } catch (e: IllegalArgumentException) {
                    Log.e(
                        "recursiveToString",
                        "Wrong number of arguments was passed to method: " + method.name
                    )
                }
            }
        }
        return result.toString()
    }

    @Throws(IllegalAccessException::class)
    fun recursiveToDict(obj: Any?): Any? {
        if (obj == null || isAndroidType(obj.javaClass)
            || isWrapperType(obj.javaClass)
        ) {
            return null
        }

        // Base case
        if (obj.javaClass.isPrimitive
            || obj.javaClass.isAssignableFrom(String::class.java)
            || obj.javaClass.isEnum
            || obj.javaClass == BigDecimal::class.java
        ) {
            return obj
        }
        val dictionary = LinkedHashMap<String, Any?>()
        val className = StringBuilder(obj.javaClass.simpleName)
        print(className)
        // Recursive step
        val fields = getDeclaredFieldsForObject(obj)
        for (field in fields) {
            field.isAccessible = true
            dictionary[field.name] = recursiveToDict(field[obj])
        }
        return dictionary
    }

    fun getPackageName(typeName: String): String {
        var childPackageName = ""

        // Get the childField's parent package name
        // Used in order to check for enum types
        val childTypeNameSplit = typeName.split("\\.").dropLastWhile { it.isEmpty() }
            .toTypedArray() // Split the package name by periods
        for (i in 0 until childTypeNameSplit.size - 1) {
            childPackageName += childTypeNameSplit[i]
            if (i < childTypeNameSplit.size - 2) {
                childPackageName += "."
            }
        }
        return childPackageName
    }

    private val WRAPPER_TYPES = wrapperTypes
    private fun isWrapperType(clazz: Class<*>): Boolean {
        return WRAPPER_TYPES.contains(clazz)
    }

    private val wrapperTypes: Set<Class<*>>
        get() {
            val ret: MutableSet<Class<*>> = HashSet()
            ret.add(Boolean::class.java)
            ret.add(Char::class.java)
            ret.add(Byte::class.java)
            ret.add(Short::class.java)
            ret.add(Int::class.java)
            ret.add(Long::class.java)
            ret.add(Float::class.java)
            ret.add(Double::class.java)
            ret.add(Void::class.java)
            ret.add(Date::class.java)
            return ret
        }

    private fun isAndroidType(obj: Any): Boolean {
        val classToString = obj.toString()
        return classToString.contains("com.android")
    }
}