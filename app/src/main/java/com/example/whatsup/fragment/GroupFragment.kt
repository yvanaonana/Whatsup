package com.example.whatsup.fragment


import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.whatsup.AppContants
import com.example.whatsup.CreerGroupActivity
import com.example.whatsup.GroupeChatActivity

import com.example.whatsup.R
import com.example.whatsup.Util.FirestoreUtil
import com.example.whatsup.recyclerview.item.GroupeItem
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.fragment_group.*
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.support.v4.startActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class GroupFragment : Fragment() {


    private lateinit var groupeListenerRegistration: ListenerRegistration
    private var shouldInitrecycleView = true
    private lateinit var poepleSection: Section

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        groupeListenerRegistration = FirestoreUtil.addSearchGroupeListener("",
            this@GroupFragment.context!!
            ,
            onListen = {
                updateRecycleView(it)
            }
        )

        var view = inflater.inflate(R.layout.fragment_group, container, false)

//        val floatingActionButton : FloatingActionButton = find(R.id.button_add_group)
//        floatingActionButton.setOnClickListener {
//            startActivity<CreerGroupActivity>()
//        }

        // Inflate the layout for this fragment
        return view
    }

    private fun click() {

    }


    @SuppressLint("MissingSuperCall")
    override fun onDestroyView() {
        super.onDestroy()
        FirestoreUtil.removeListener(groupeListenerRegistration)
        shouldInitrecycleView = true
    }

    private fun updateRecycleView(items: List<Item>) {

        fun init() {
            recycler_view_group.apply {
                layoutManager = LinearLayoutManager(this@GroupFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    poepleSection = Section(items)
                    add(poepleSection)
                    setOnItemClickListener(onItemClick)
                }
            }
            shouldInitrecycleView = false
        }

        fun updateItems() = poepleSection.update(items)

        if (shouldInitrecycleView) {
            try {
                init()
            }catch (e: Exception){
                Log.e("Groupefragent", "Erreur Null: "+e.message)
            }
        } else
            updateItems()

    }

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is GroupeItem) {
            startActivity<GroupeChatActivity>(
                AppContants.ID_GROUPE to item.chatGroupeId,
                AppContants.NOM_GROUPE to item.chatGroupe.groupeName,
                AppContants.NOMBRE_MEMBRE_GROUPE to item.chatGroupe.members?.size.toString()
            )

        }
    }
}
