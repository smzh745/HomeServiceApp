package home.service.appmanage.online.work.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.models.SubType
import kotlinx.android.synthetic.main.sub_type_layout.view.*

class SubTypeAdapter(
    private val context: Context, private val serviceList: ArrayList<SubType>
) : RecyclerView.Adapter<SubTypeAdapter.MyHolder>() {


    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sub_type_layout, parent, false)
        return MyHolder(view!!)

    }

    override fun getItemCount(): Int {
        return serviceList.size
    }

    override fun onBindViewHolder(p0: MyHolder, p1: Int) {
        val service = serviceList[p1]
        p0.itemView.text.isSelected = true
        p0.itemView.text.text = service.text
        p0.itemView.cardCLick.setOnClickListener {
            openView(service, it)
        }
        p0.itemView.icon.setOnClickListener {
            openView(service, it)

        }
    }

    private fun openView(
        service: SubType,
        it: View
    ) {
        val bundle = bundleOf("type" to service.type, "subType" to service.text)
        findNavController(it).navigate(R.id.subTypeDetailsFragment, bundle)
    }
}