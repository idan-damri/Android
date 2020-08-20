package com.example.easywedding;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easywedding.model.Contact;
import com.example.easywedding.model.Guest;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>
implements Filterable {

    private Context mContext;
    // Original items
    private List<Contact> mContacts;
    // Copy of the original list
    private List<Contact> mContactsCopy;

    public ContactsAdapter(Context mContext, List<Contact> mContacts) {
        this.mContext = mContext;
        this.mContacts = mContacts;
    }


    @NonNull
    @Override
    public ContactsAdapter.ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recyclerview_contact_item, parent, false);

        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsAdapter.ContactViewHolder holder, int position) {
        final Contact contact = mContacts.get(position);

        if (contact.isInvited())
            holder.checkBox.setChecked(true);
        else
            holder.checkBox.setChecked(false);

        holder.name.setText(contact.getName());
        // TODO if this user check, then update mContacts here to checked (listener)
        // TODO Then here in view holder,if it checked in mContacts check it here.

        String photo = contact.getPhoto();
        if (photo != null)
            Picasso.get().load(photo).into(holder.image);
        else
            holder.image.setImageResource(R.drawable.contact_image);

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // If I scroll the list then it will uncheck checked checkboxes
                // so I used isPressed().
                if (buttonView.isPressed())
                    contact.setInvited(isChecked);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    @Override
    public Filter getFilter() {
        return mContactsFilter;
    }
    private Filter mContactsFilter = new Filter() {
        // Asynchronous method.
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Contact> filteredContactList = new ArrayList<>();
            // If the user didn't gave a filter string pattern
            // then show the user all the contacts, which are always located
            // mContactCopy
            if (constraint == null || TextUtils.isEmpty(constraint)){
                filteredContactList.addAll(mContactsCopy);
            }
            else{
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Contact contact : mContactsCopy){
                    // If the current contact name contains the required string pattern
                    // then add the contact to the filtered contact list.
                    if (contact.getName().toLowerCase().contains(filterPattern)){
                        filteredContactList.add(contact);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredContactList;

            return filterResults;
        }
        // Published to UI thread.
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // mContacts is the list the we set the adapter to.
            mContacts.clear();
            mContacts.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    /**
     * This method gets called when
     * the original contacts list (mContacts) contains the contacts of the user
     */
    public void initializeContactsCopyList() {
        mContactsCopy = new ArrayList<>(mContacts);
        return;
    }

    public static class ContactViewHolder extends  RecyclerView.ViewHolder{

        TextView name;
        CircleImageView image;
        MaterialCheckBox checkBox;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.list_item_guest_name);
            image = itemView.findViewById(R.id.contact_image);
            checkBox = itemView.findViewById(R.id.checkbox_list_item);

        }
    }
    public List<Contact> getContactsCopy (){
        return mContactsCopy;
    }

    public List<Guest> getGuestsFromContacts(){
        if (mContactsCopy == null)
            return null;

        List<Guest> guests = new ArrayList<>();

        for (Contact contact : mContactsCopy){
            if (contact.isInvited())
                guests.add(contact.getGuestInstanceFromContact());
        }
        return guests;
    }

}
