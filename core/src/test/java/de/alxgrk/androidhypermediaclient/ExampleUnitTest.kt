package de.alxgrk.androidhypermediaclient

import io.kotlintest.fail
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.jupiter.api.Test


class ExampleUnitTest {

    val jsonResponse = """
        {
          "lineItems" : [ {
            "name" : "Java Chip",
            "quantity" : 1,
            "milk" : "Low-fat",
            "size" : "Large",
            "price" : "EUR4.20"
          } ],
          "location" : "To go",
          "orderedDate" : "2019-07-22T09:48:40.974",
          "status" : "Ready",
          "price" : "EUR4.20",
          "_links" : {
            "self" : {
              "href" : "http://localhost:8080/orders/1"
            },
            "test:order" : {
              "href" : "http://localhost:8080/orders/1{?projection}",
              "templated" : true,
              "title" : "An order"
            },
            "curies" : [ {
              "href" : "http://localhost:8080/docs/{rel}.html",
              "name" : "test",
              "templated" : true
            } ]
          }
        }
    """.trimIndent()

    @Test
    fun `links are correctly parsed`() {
        val uut = HypermediaRemoteResource(jsonResponse)

        uut.getLink("self").toString() shouldContain "http://localhost:8080/orders/1"

        uut.getLink("unknown") shouldBe null
    }

    @Test
    fun `callbacks are executed if links are present`() {
        val uut = HypermediaRemoteResource(jsonResponse)

        var executed = false
        uut.ifPresent("self") {
            executed = true
        }
        executed shouldBe true

        uut.ifPresent("unknown") {
            fail("this shouldn't be executed")
        }
    }

    @Test
    fun `proxy creation for OrderDetails works`() {
        val uut = HypermediaRemoteResource(jsonResponse)

        val orderDetails = uut.getPayloadAs(OrderDetails::class)
        orderDetails.getOrderedDate() shouldBe "2019-07-22T09:48:40.974"
        orderDetails.getStatus() shouldBe "Ready"
    }

    @Test
    fun `proxy creation for SomethingElse fails`() {
        val uut = HypermediaRemoteResource(jsonResponse)

        val somethingElse = uut.getPayloadAs(SomethingElse::class)
        // FIXME
        // somethingElse.getSomethingElse() shouldNotBe null
    }

    interface OrderDetails {

        fun getOrderedDate(): String

        fun getStatus(): String
    }

    interface SomethingElse {

        fun getSomethingElse(): String
    }
}