package com.bookpalace.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bookpalace.app.databinding.ItemStudentNotificationBinding
import com.bookpalace.app.viewmodel.StudentSelectionItem

class StudentNotificationAdapter(
    private val onCheckedChange: (studentId: String, isChecked: Boolean) -> Unit
) : ListAdapter<StudentSelectionItem, StudentNotificationAdapter.StudentViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StudentViewHolder(
        private val binding: ItemStudentNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StudentSelectionItem) {
            val user = item.user

            binding.tvStudentName.text = user.name.ifBlank { "Unknown" }
            binding.tvStudentEmail.text = user.email.ifBlank { "No email" }

            // Suppress listener before setting checked state to avoid feedback loop
            binding.cbStudent.setOnCheckedChangeListener(null)
            binding.cbStudent.isChecked = item.isSelected

            // Toggle on card tap OR checkbox tap
            binding.root.setOnClickListener {
                val newChecked = !binding.cbStudent.isChecked
                binding.cbStudent.isChecked = newChecked
                onCheckedChange(user.id, newChecked)
            }

            binding.cbStudent.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChange(user.id, isChecked)
            }

            // Highlight selected card
            binding.root.strokeColor = if (item.isSelected) {
                itemView.context.getColor(android.R.color.holo_blue_light)
            } else {
                itemView.context.getColor(android.R.color.transparent)
            }
            binding.root.strokeWidth = if (item.isSelected) 4 else 1
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StudentSelectionItem>() {
            override fun areItemsTheSame(
                oldItem: StudentSelectionItem,
                newItem: StudentSelectionItem
            ) = oldItem.user.id == newItem.user.id

            override fun areContentsTheSame(
                oldItem: StudentSelectionItem,
                newItem: StudentSelectionItem
            ) = oldItem == newItem
        }
    }
}
