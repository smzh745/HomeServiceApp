package home.service.appmanage.online.work.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.models.Service
import kotlinx.android.synthetic.main.service_layout.view.*

class ServiceAdapter(
    private val context: Context, private val serviceList: ArrayList<Service>
) : RecyclerView.Adapter<ServiceAdapter.MyHolder>() {


    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.service_layout, parent, false)
        return MyHolder(view!!)

    }

    override fun getItemCount(): Int {
        return serviceList.size
    }

    override fun onBindViewHolder(p0: MyHolder, p1: Int) {
        val service = serviceList[p1]
        p0.itemView.textView.isSelected = true
        p0.itemView.textView.text = service.title
        Glide.with(context).load(service.image).into(p0.itemView.imageView)
    }
}