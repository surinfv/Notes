package com.fed.notes.view.list

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fed.notes.R
import com.fed.notes.database.Entity.Note
import com.fed.notes.presenter.ListPresenter
import com.fed.notes.utils.inflate
import com.fed.notes.utils.loadOrder
import com.fed.notes.utils.saveOrder
import com.fed.notes.view.MainActivity
import com.fed.notes.view.touchhelper.ItemTouchHelperAdapter
import com.fed.notes.view.touchhelper.SimpleItemTouchHelperCallback
import kotlinx.android.synthetic.main.fragment_note_list.*
import kotlinx.android.synthetic.main.list_item.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ListFragment : Fragment(), ListInterface {

    private lateinit var presenter: ListPresenter
    private var adapter: NoteAdapter? = null
    private lateinit var notesOrder: MutableList<UUID>


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_note_list)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        note_recycler_view.layoutManager = LinearLayoutManager(activity)
        fab_add_note.setIcon(R.drawable.ic_new_note)
        fab_add_note.setOnClickListener { (presenter.createNewNoteClicked()) }


        adapter = NoteAdapter(ArrayList())
        val callback = SimpleItemTouchHelperCallback(adapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(note_recycler_view)
    }

    override fun onResume() {
        super.onResume()
        loadListOrder()
        presenter = ListPresenter()
        presenter.init()
        presenter.attach(this)
        presenter.updateUI(notesOrder)
    }

    override fun onPause() {
        super.onPause()
        presenter.onViewPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detach()
    }

    override fun setNotes(notes: ArrayList<Note>) {
        if (adapter == null) adapter = NoteAdapter(notes)
        note_recycler_view.adapter = adapter
        adapter?.setNotes(notes)
        adapter?.notifyDataSetChanged()
    }

    override fun openNoteEditorFragment(note: Note) {
        (activity as MainActivity).openNoteFragmentEditor(note)
    }

    override fun addNoteToOrder(noteId: UUID) {
        notesOrder.add(0, noteId)
    }

    override fun saveListOrder() {
        saveOrder(notesOrder, context)
    }

    private fun loadListOrder() {
        notesOrder = loadOrder(context)
    }

    private inner class NoteAdapter internal constructor(private var notes: ArrayList<Note>) : RecyclerView.Adapter<NoteHolder>(), ItemTouchHelperAdapter {

        private var noteTmpPos: Int = 0
        private lateinit var noteTmp: Note
        var snackBarOnClickListener: View.OnClickListener = View.OnClickListener {
            ///return just deleted note
            Toast.makeText(activity, noteTmp.title + resources.getString(R.string.snackbar_return), Toast.LENGTH_SHORT).show()
            notes.add(noteTmpPos, noteTmp)
            notesOrder.add(noteTmpPos, noteTmp.id)
            notifyItemInserted(noteTmpPos)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
            val inflater = LayoutInflater.from(activity)
            val view = inflater.inflate(R.layout.list_item, parent, false)
            return NoteHolder(view)
        }

        override fun onBindViewHolder(holder: NoteHolder, position: Int) {
            val note = notes[position]
            holder.bindNote(note)
        }

        override fun getItemCount(): Int {
            return notes.size
        }

        fun setNotes(notes: ArrayList<Note>) {
            this.notes = notes
        }

        override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(notes, i, i + 1)
                    Collections.swap(notesOrder, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(notes, i, i - 1)
                    Collections.swap(notesOrder, i, i - 1)
                }
            }
            notifyItemMoved(fromPosition, toPosition)
            return true
        }

        override fun onItemDismiss(position: Int) {
            cloneNote(notes[position], position)

            notes.removeAt(position)
            notesOrder.removeAt(position)
            notifyItemRemoved(position)

            val snackBar = Snackbar.make(note_recycler_view, noteTmp.title + resources.getString(R.string.snackbar_delete), Snackbar.LENGTH_LONG)
            val snackBarView = snackBar.view
            snackBarView.setBackgroundColor(ContextCompat.getColor(context, R.color.snack_bar_background))
            snackBar.setAction(resources.getString(R.string.snackbar_undo), snackBarOnClickListener)
            snackBar.show()

            snackBar.addCallback(object : Snackbar.Callback() {
                override fun onDismissed(snackBar: Snackbar?, event: Int) {
                    if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                        presenter.deleteNote(noteTmp)
                    }
                }
            })
        }

        private fun cloneNote(note: Note?, pos: Int) {
            noteTmpPos = pos
            noteTmp = Note()
            noteTmp.title = note?.title
            noteTmp.description = note?.description
            noteTmp.id = note!!.id
            noteTmp.date = note.date
        }
    }

    private inner class NoteHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        private lateinit var note: Note

        fun bindNote(note: Note?) {
            if (note == null) return
            this.note = note
            itemView.item_list_title.text = this.note.title
            itemView.item_list_description.text = this.note.description
            val dateFormat = SimpleDateFormat(getString(R.string.date_format), Locale.ENGLISH)
            itemView.item_list_date.text = dateFormat.format(this.note.date)
        }

        override fun onClick(v: View) = (activity as MainActivity).openNoteFragmentPreview(note)
    }
}