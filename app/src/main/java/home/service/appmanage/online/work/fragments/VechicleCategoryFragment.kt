package home.service.appmanage.online.work.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import home.service.appmanage.online.work.R
import kotlinx.android.synthetic.main.fragment_vechicle_category.view.*


class VechicleCategoryFragment : BaseFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_vechicle_category, container, false)

        root!!.bikee.setOnClickListener {
            navigateFragmentbyType(
                R.id.confirmPickUpLocation,
                getString(R.string.bike)
            )
        }
        root!!.go1.setOnClickListener {
            navigateFragmentbyType(
                R.id.confirmPickUpLocation,
                getString(R.string.go)
            )
        }
        root!!.goMini.setOnClickListener {
            navigateFragmentbyType(
                R.id.confirmPickUpLocation,
                getString(R.string.go_mini)
            )
        }
        root!!.goPlus.setOnClickListener {
            navigateFragmentbyType(
                R.id.confirmPickUpLocation,
                getString(R.string.go_)
            )
        }
        return root
    }


}