package com.example.whatsup.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.whatsup.AppContants
import com.example.whatsup.ChatActivity


import com.example.whatsup.R
import com.example.whatsup.Util.FirestoreUtil
import com.example.whatsup.recyclerview.item.PersonItem

import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener


import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.fragment_people.*
import org.jetbrains.anko.support.v4.startActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class PeopleFragment : Fragment() {

    private lateinit var userListenerRegistration : ListenerRegistration

    private var shouldInitRecyclerView = true

    private lateinit var peopleSection : Section

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        userListenerRegistration = FirestoreUtil.addUsersListener(this.activity!!, this::updateRecyclerView)

        return inflater.inflate(R.layout.fragment_people, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        FirestoreUtil.removeListener(userListenerRegistration)
        shouldInitRecyclerView = true
    }

    private fun updateRecyclerView(items : List<Item>){

        fun init(){
            recycler_view_people.apply {
                layoutManager = LinearLayoutManager(this@PeopleFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    peopleSection = Section(items)
                    add(peopleSection)
                    setOnItemClickListener(onItemClick)
                }
            }
            shouldInitRecyclerView = false
        }

        fun updateItems() = peopleSection.update(items)

        if(shouldInitRecyclerView)
            init()
        else
            updateItems()
    }

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is PersonItem){
            startActivity<ChatActivity>(
                AppContants.USER_NAME to item.person.name,
                AppContants.USER_ID to item.userId
            )
        }
    }

}
