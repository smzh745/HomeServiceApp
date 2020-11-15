package home.service.appmanage.online.work.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.activities.BaseActivity
import home.service.appmanage.online.work.activities.PlayVideoActivity
import home.service.appmanage.online.work.models.TypeDetails
import home.service.appmanage.online.work.utils.Constants.TAGI
import kotlinx.android.synthetic.main.worker_type_details_layout.view.*

class TypeDetailsAdapter(
    private val context: Context, private val serviceList: ArrayList<TypeDetails>
) : RecyclerView.Adapter<TypeDetailsAdapter.MyHolder>() {


    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.worker_type_details_layout, parent, false)
        return MyHolder(view!!)

    }

    override fun getItemCount(): Int {
        return serviceList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(p0: MyHolder, p1: Int) {
        val service = serviceList[p1]

        p0.itemView.typeTitle.text = service.typeTitle
        p0.itemView.typeDesp.text = service.typeDesp
        p0.itemView.typePrice.text = service.currency + " " + service.fare
        if (service.isVideo == 1) {
            p0.itemView.viewSample.visibility = View.VISIBLE
        } else {
            p0.itemView.viewSample.visibility = View.GONE

        }
        p0.itemView.detailsClickBtn.setOnClickListener {
            openDetails(service, p1)
        }
        p0.itemView.view.setOnClickListener {
            openDetails(service, p1)
        }
        p0.itemView.viewSample.setOnClickListener {
            if (service.type.equals(context.getString(R.string.photography_event), true)) {
                Log.d(TAGI, "onBindViewHolder:nothing ")
                playVideo(service)
            }
        }
    }

    private fun openDetails(
        service: TypeDetails,
        p1: Int
    ) {
        if (service.type.equals(context.getString(R.string.photography_event), true)) {
            Log.d(TAGI, "onBindViewHolder:nothing ")
            playVideo(service)
        } else {
            val bundle = Bundle()
            bundle.putInt("position", p1)
            bundle.putParcelableArrayList("typed", serviceList)
            (context as BaseActivity).navigateFragment(R.id.bookWorkerFragment, bundle)
        }
    }

    private fun playVideo(service: TypeDetails) {
        if (service.isVideo == 1) {
            val intent = Intent(context, PlayVideoActivity::class.java)
            intent.putExtra("videoUrl", service.video)
            context.startActivity(intent)
        }
    }
}