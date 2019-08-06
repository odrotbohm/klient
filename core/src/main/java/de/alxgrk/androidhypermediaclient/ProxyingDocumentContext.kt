package de.alxgrk.androidhypermediaclient

import com.fasterxml.jackson.databind.ObjectMapper
import com.googlecode.openbeans.Introspector
import com.jayway.jsonpath.*
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider
import org.aopalliance.intercept.MethodInterceptor
import org.json.JSONObject
import org.springframework.aop.framework.ProxyFactory
import org.springframework.util.ConcurrentReferenceHashMap
import java.lang.reflect.Method
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentMap
import kotlin.reflect.KClass

internal class ProxyingDocumentContext(
    response: String,
    private val documentContext: DocumentContext = JsonPath.parse(response, configuration)
) : DocumentContext by documentContext {

    companion object {
        private val objectMapper = ObjectMapper()

        private val configuration = Configuration.builder()
            .mappingProvider(JacksonMappingProvider(objectMapper))
            .build()

        private val cache: ConcurrentMap<Method, JsonPath> = ConcurrentReferenceHashMap()
    }

    fun <T : Any> getProxy(type: KClass<T>) = JsonPathProxyFactory(this, cache).getProxy(type)

    fun readAsMap(path: String): Map<String, String> = this.read(path, object : TypeRef<Map<String, String>>() {})

    fun readArrayAsStringList(path: String): List<String> = this.read(path, object : TypeRef<List<Map<Any, Any>>>() {})
        .map { JSONObject(it).toString() }

}

private class JsonPathProxyFactory(
    val context: DocumentContext,
    val cache: ConcurrentMap<Method, JsonPath>
) {

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getProxy(type: KClass<T>): T {

        val methods = Introspector.getBeanInfo(type.java).propertyDescriptors

        return ProxyFactory().run {
            setInterfaces(type.java)
            addAdvice(MethodInterceptor { invocation ->

                val method = invocation.method

                if (method.name === "toString") {
                    return@MethodInterceptor context.jsonString()
                }

                val returnType = object : TypeRef<T>() {
                    override fun getType(): Type = method.genericReturnType
                }

                val jsonPath = cache.getOrPut(method) {

                    methods.filter { it.readMethod == method }
                        .mapNotNull { JsonPath.compile("$.${it.name}") }
                        .first()
                }

                try {
                    context.read(jsonPath, returnType)
                } catch (e: PathNotFoundException) {
                    // FIXME is it really ok to return null, although a type might promise to never do so?
                    null
                }
            })

            getProxy(type.java.classLoader) as T
        }
    }

}