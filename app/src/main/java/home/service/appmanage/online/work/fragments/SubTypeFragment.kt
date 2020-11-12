package home.service.appmanage.online.work.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.adapters.SubTypeAdapter
import home.service.appmanage.online.work.models.SubType
import kotlinx.android.synthetic.main.fragment_sub_type.view.*

class SubTypeFragment : BaseFragment() {
    private var subList: ArrayList<SubType>? = null
    private var adapter: SubTypeAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            requireArguments().getString("type")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_sub_type, container, false)
        subList = ArrayList()
        subList!!.clear()
        if (requireArguments().getString("type")
                .equals(getString(R.string.photography_event), true)
        ) {
            root!!.detailsClickBtn.visibility = View.VISIBLE
        }
        root!!.detailsClickBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:" + getString(R.string._92317558327))
            startActivity(intent)
        }
        if (requireArguments().getString("type").equals(getString(R.string.ac_service))) {
            adapter = SubTypeAdapter(requireActivity(), getAcList())
        } else if (requireArguments().getString("type")
                .equals(getString(R.string.photography_event))
        ) {
            adapter = SubTypeAdapter(requireActivity(), getEventList())

        }
        //now adding the adapter to recyclerview
        root!!.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
//        root!!.recyclerView.addItemDecoration(GridSpacingItemDecoration(2, 30, false))
        root!!.recyclerView.adapter = adapter
        return root
    }

    private fun getAcList(): ArrayList<SubType> {
        subList!!.add(
            SubType(
                getString(R.string.ac_inverter),
                requireArguments().getString("type").toString()
            )
        )
        subList!!.add(
            SubType(
                getString(R.string.ac_split),
                requireArguments().getString("type").toString()
            )
        )
        subList!!.add(
            SubType(
                getString(R.string.window_ac),
                requireArguments().getString("type").toString()
            )
        )
        subList!!.add(
            SubType(
                getString(R.string.standing_ac),
                requireArguments().getString("type").toString()
            )
        )
        subList!!.add(
            SubType(
                getString(R.string.cast_ac),
                requireArguments().getString("type").toString()
            )
        )
        return subList!!
    }

    private fun getEventList(): ArrayList<SubType> {
        subList!!.add(
            SubType(
                getString(R.string.movie_hd),
                requireArguments().getString("type").toString()
            )
        )
        subList!!.add(
            SubType(
                getString(R.string.movie_hr),
                requireArguments().getString("type").toString()
            )
        )
        subList!!.add(
            SubType(
                getString(R.string.photo_shoot),
                requireArguments().getString("type").toString()
            )
        )
        subList!!.add(
            SubType(
                getString(R.string.events),
                requireArguments().getString("type").toString()
            )
        )
        return subList!!
    }
}