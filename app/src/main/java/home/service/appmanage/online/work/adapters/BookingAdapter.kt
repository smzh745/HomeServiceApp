package home.service.appmanage.online.work.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.models.Booking
import kotlinx.android.synthetic.main.book_layout.view.*

class BookingAdapter(private val serviceList: ArrayList<Booking>
) : RecyclerView.Adapter<BookingAdapter.MyHolder>() {


    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.book_layout, parent, false)
        return MyHolder(view!!)

    }

    override fun getItemCount(): Int {
        return serviceList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(p0: MyHolder, p1: Int) {
        val service = serviceList[p1]
        p0.itemView.bookId.text = "ID# " + service.bookId
        p0.itemView.serviceType.text = service.workedType
        p0.itemView.date.text = service.bookedAt
        p0.itemView.fare.text = service.currency + " " + service.fare

    }


}