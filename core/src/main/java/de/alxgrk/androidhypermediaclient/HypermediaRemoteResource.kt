package de.alxgrk.androidhypermediaclient

import com.jayway.jsonpath.PathNotFoundException
import org.json.JSONObject
import org.springframework.hateoas.Link
import kotlin.reflect.KClass

data class HypermediaRemoteResource(val response: String) {

    private val context = ProxyingDocumentContext(response)

    fun <T : Any> getPayloadAs(type: KClass<T>): T =
        context.getProxy(type)

    /**
     * Returns the Link with the given relation.
     */
    fun getLink(relation: String): Link? =
        try {
            Link(context.readAsMap("$._links.$relation")["href"], relation)
        } catch (e: PathNotFoundException) {
            null
        }

    /**
     * Executes the given callback if a link with the given relation is present.
     */
    fun ifPresent(relation: String, callback: (Link) -> Unit) = getLink(relation)?.let(callback)

    fun getEmbedded(relation: String): List<HypermediaRemoteResource> =
        context.readArrayAsStringList("$._embedded.$relation")
            .map { HypermediaRemoteResource(it) }

}