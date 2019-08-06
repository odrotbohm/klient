package de.alxgrk.androidhypermediaclient

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.alxgrk.androidhypermediaclient.model.OrderPreview
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.order_list_item.view.*
import org.springframework.hateoas.Link

class MainActivity : AppCompatActivity() {

    val singleOrder = """
            {
              "id": "1",
              "status" : "Ready",
              "orderedDate" : "2019-07-22T09:48:40.974",
              "_links" : {
                "self" : {
                  "href" : "http://localhost:8080/orders/1"
                },
                "restbucks:order" : {
                  "href" : "http://localhost:8080/orders/1{?projection}",
                  "templated" : true,
                  "title" : "An order"
                }
              }
            }
    """.trimIndent()

    val orders = """
        {
          "_embedded": {
            "restbucks:orders": [
              {
                "id": "1",
                "status": "Ready",
                "_links": {
                  "self": {
                    "href": "http://localhost:8080/orders/1"
                  },
                  "restbucks:order": {
                    "href": "http://localhost:8080/orders/1{?projection}",
                    "templated": true,
                    "title": "An order"
                  }
                }
              },
              {
                "id": "2",
                "status": "Payment expected",
                "_links": {
                  "self": {
                    "href": "http://localhost:8080/orders/2"
                  },
                  "restbucks:order": {
                    "href": "http://localhost:8080/orders/2{?projection}",
                    "templated": true,
                    "title": "An order"
                  }
                }
              }
            ]
          },
          "_links": {
            "self": {
              "href": "http://localhost:8080/orders{?page,size,sort,projection}",
              "templated": true
            },
            "profile": {
              "href": "http://localhost:8080/profile/orders"
            },
            "search": {
              "href": "http://localhost:8080/orders/search"
            },
            "curies": [
              {
                "href": "http://localhost:8080/docs/{rel}.html",
                "name": "restbucks",
                "templated": true
              }
            ]
          },
          "page": {
            "size": 20,
            "totalElements": 2,
            "totalPages": 1,
            "number": 0
          }
        }
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // configure basic resource handling
        val resource = HypermediaRemoteResource(orders)
        resource.ifPresent(Link.REL_SELF) {
            btn_reload.visibility = View.VISIBLE
            btn_reload.setOnClickListener {
                recreate()
            }
        }
        NavUtils.getParentActivityIntent(this)?.let {
            btn_back.visibility = View.VISIBLE
            btn_back.setOnClickListener {
                NavUtils.navigateUpTo(this, intent)
            }
        }

        // configure order list
        val embedded = resource.getEmbedded("restbucks:orders")
        order_list_view.adapter = SimpleItemRecyclerViewAdapter(embedded)

        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        order_list_view.addItemDecoration(divider)

        order_list_view.layoutManager = LinearLayoutManager(this)
        order_list_view.setHasFixedSize(true)

    }

    class SimpleItemRecyclerViewAdapter(
        private val values: List<HypermediaRemoteResource>
    ) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.order_list_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val item = values[position]
            val summary = item.getPayloadAs(OrderPreview::class)

            holder.idView.text = summary.getId()
            holder.contentView.text = summary.getStatus()

            with(holder.itemView) {
                tag = item
                setOnClickListener {
                    Log.d("MAIN", "clicked $position")
                }
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.id_text
            val contentView: TextView = view.content
        }

    }
}
