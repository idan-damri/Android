package com.example.easywedding;


import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;


import com.example.easywedding.Utils.Utils;
import com.example.easywedding.model.Message;
import com.example.easywedding.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;

import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private EditText mMessageEditText;
    private Button mSendMessageButton;
    private View mEmptyChatLayout;
    private View mFragmentLayout;

    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserGroupChatReference;
    private DatabaseReference mChatsReference;
    private DatabaseReference mUserReference;
    private DatabaseReference mInvitesReceivedReference;
    private DatabaseReference mUserChatIdentifier;
    private ValueEventListener mUserChatIdentifierListener;
    private ChildEventListener mInvitesListener;
    private LinearLayoutManager mLinearLayoutManager;

    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mAdapter;

    private String mChatId;
    private boolean mIsAllowedToGetInvites;



    // default length limit of text in an EditText object
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    public static final String TAG = ChatFragment.class.getSimpleName();

    private final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    private final SimpleDateFormat chatDateFormat = new SimpleDateFormat("dd-MM-yyy"
            ,Locale.getDefault());
    private final SimpleDateFormat timeChatFormat = new SimpleDateFormat("HH:mm",
            Locale.getDefault());

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        // indicates that this fragment has an options menu
        setHasOptionsMenu(true);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        mFragmentLayout = inflater.inflate(R.layout.fragment_chat, container, false);

        mRecyclerView = mFragmentLayout.findViewById(R.id.recyclerview_chat);
        mMessageEditText = mFragmentLayout.findViewById(R.id.message_edittext);
        mSendMessageButton = mFragmentLayout.findViewById(R.id.send_message_button);
       // mEmptyChatLayout = mFragmentLayout.findViewById(R.id.empty_chat_layout);

        // TODO uncomment after finished implementation
        //setEmptyChatLayoutClickListener();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mChatsReference = mFirebaseDatabase.getReference().child(Constants.PATH_CHATS);
        // get a reference to the current user node in the db
        mUserReference = mFirebaseDatabase.getReference()
                .child(Constants.PATH_USERS)
                .child(mFirebaseUser.getUid());

        mInvitesReceivedReference = mUserReference.child(Constants.PATH_INVITES_RECEIVED);

        mUserChatIdentifier = mUserReference.child(Constants.PATH_CHAT_ID);



        return mFragmentLayout;
    }

    private ValueEventListener setUserChatIdentifierListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mChatId = snapshot.getValue(String.class);
                    // Show chat messages and disable chat invites only if there exists a chat.
                    if (mChatId != null) {
                        mUserChatIdentifier.removeEventListener(mUserChatIdentifierListener);
                        mUserChatIdentifierListener = null;
                        ListenToMessagesInChat();
                        // Only one chat is allowed to a user
                        disableAbilityToGetInvites();

                        // Enable the user to enter and deploy a message in the chat
                        handleMessageEditText();
                        handleMessageDeploy();

                        onLayoutChange(false);

                    }
                    // The user don't have a chat so enable him to get chat invites.
                    else {
                        enableAbilityToGetInvites();
                        mMessageEditText.setEnabled(false);
                        mSendMessageButton.setEnabled(false);
                        onLayoutChange(true);
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "error listening to chat id record in user " + error.toException());

            }
        };
    }

    private ChildEventListener setInvitesListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                final String chatId = snapshot.getKey();
                String senderEmail = snapshot.getValue(String.class);

                // TODO create a function for the dialog creation
                new MaterialAlertDialogBuilder(getActivity())
                        .setTitle(R.string.chat_invite_received)
                        .setMessage(senderEmail)
                        .setPositiveButton(R.string.join_chat, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                addUserToChat(chatId);
                            }
                        }).setNegativeButton(R.string.decline_invite, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Remove the invite for the chat invites record in the db.
                        mUserReference.child(Constants.PATH_INVITES_RECEIVED).child(chatId).removeValue();
                    }
                }).create().show();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "can't listen to invites " + error.toException());
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();

            // If the user don't have a chat then listen to when the user will have a new chat
            if (mUserChatIdentifierListener == null && mChatId == null) {
                mUserChatIdentifierListener = setUserChatIdentifierListener();
                mUserChatIdentifier.addValueEventListener(mUserChatIdentifierListener);
            }

            if(mAdapter != null)
                mAdapter.startListening();

    }

    private void ListenToMessagesInChat() {
        // Get a reference to the user's chat.
        mUserGroupChatReference = mChatsReference.child(mChatId);

        FirebaseRecyclerOptions<Message> options = new FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(mUserGroupChatReference, Message.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull Message model) {


                String text = model.getText();
                if (text != null)
                    holder.text.setText(text);

                // Parse the time
                Long timeMillis = model.getTime();
                if (timeMillis != null){
                    Date date = new Date(timeMillis);
                    holder.time.setText(timeChatFormat.format(date));
                }

                String currentMessageSenderId = model.getSenderId();

                if (currentMessageSenderId != null) {
                    // If this message belong to the sender  the se the sender background
                    if (currentMessageSenderId.equals(mFirebaseUser.getUid())) {
                        holder.name.setVisibility(View.GONE);
                        holder.itemView.setBackground(getResources().getDrawable(R.drawable.sender_message_background));
                        // Else this message doesn't belong to the current user so display the name of the sender
                        // No need to display the current user name if he sent the message.
                    }
                    else {
                        holder.name.setVisibility(View.VISIBLE);
                        holder.name.setText(model.getName());
                        holder.itemView.setBackground(getResources().getDrawable(R.drawable.others_message_background));
                    }
                }

            }

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // The ViewHolder will keep references to the views in the list item view.
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_chat, parent, false);
                return new MessageViewHolder(view);
            }

        };
         mLinearLayoutManager = new LinearLayoutManager(getContext());
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        // According to Google codelabs. Always scroll to bottom.
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });


        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();

        if (mAdapter != null)
            mAdapter.stopListening();

            // Stop listening to if the user have a chat.
            if (mUserChatIdentifierListener != null) {
                mUserChatIdentifier.removeEventListener(mUserChatIdentifierListener);
                mUserChatIdentifierListener = null;
            }

            // then stop listening to chat invites
            if (mInvitesListener != null) {
                mInvitesReceivedReference.removeEventListener(mInvitesListener);
                mInvitesListener = null;
            }


    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder{

        TextView name, time, text;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name_textview);
            time = itemView.findViewById(R.id.time_textview);
            text = itemView.findViewById(R.id.message_textview);
        }
    }
    /*
    TODO continue implementing, this shows an interface for inviting a user by email
    private void setEmptyChatLayoutClickListener() {
        mEmptyChatLayout.findViewById(R.id.empty_chat_imageview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createInviteToChatDialog();
            }
        });
    }

     */





    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem actionOpen = menu.findItem(R.id.action_open_chat);
        MenuItem actionInvite = menu.findItem(R.id.action_invite_chat);
        MenuItem actionLeave = menu.findItem(R.id.action_leave_chat);

        // If the user don't have a chat open so show only relevant menu items
        if (mChatId == null || TextUtils.isEmpty(mChatId)) {
            if (actionInvite.isVisible()) {
                actionInvite.setVisible(false);
                actionLeave.setVisible(false);
            }
            actionOpen.setVisible(true);

        } else {
            if (actionOpen.isVisible())
                actionOpen.setVisible(false);

            actionInvite.setVisible(true);
            actionLeave.setVisible(true);
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.chat_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_leave_chat:
                // Create an a UI that the user can interact with for leaving the chat
                createLeaveChatDialog();
                return true;
            case R.id.action_open_chat:
                // Create a unique chat for the user.
                addNewChatToDatabase();
                return true;
            case R.id.action_invite_chat:
                // create a UI the user can interact with for inviting another user to chat
                createChatInviteDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createLeaveChatDialog() {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.alert_leave_chat)
                .setPositiveButton(R.string.alert_positive_leave_chat, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeUserFromChat();
                    }
                }).setNegativeButton(R.string.alert_negative_leave_chat, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // No action
            }
        }).create().show();
    }

    /**
     * Create a UI that allowing the user to send invites to the chat.
     */
    private void createChatInviteDialog() {
        ConstraintLayout emailInputLayout = (ConstraintLayout) LayoutInflater.from(getActivity()).inflate(R.layout.dialog_chat_invite, null);
        final TextInputLayout inputLayout =emailInputLayout.findViewById(R.id.dialog_email_input_layout);
        final TextInputEditText emailInput = emailInputLayout.findViewById(R.id.dialog_input_email);
        final String errorMessage = getString(R.string.error_chat_email_input);

        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                inputLayout.setError("");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        // Click listener for the positive button is null since I will
        // implement my own behavior for deciding when to dismiss the dialog.
        builder.setTitle(R.string.chat_invite_email_title)
                .setPositiveButton(R.string.dialog_positive_invite_to_chat
                , null)
                .setNegativeButton(R.string.dialog_negative_invite_to_chat
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Intentionally left blank
                            }
                        })
                .setView(emailInputLayout);

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the email is valid then add the user to the chat.
                if (Utils.isValidEmail(emailInput.getText())) {
                    addUserToChatByEmail(emailInput.getText().toString());
                    dialog.dismiss();
                    // Else show an error message and don't dismiss the dialog.
                } else
                    inputLayout.setError(errorMessage);
            }
        });
    }



    // TODO delete this and implement the behavior with a Dynamic Link
    // TODO or create menu item for allowing chat to specific email. Implement with dialog
    private void addUserToChatByEmail(final String targetEmail) {
        Query query = mFirebaseDatabase.getReference().child(Constants.PATH_USERS)
                .orderByChild(Constants.ORDER_BY_EMAIL).equalTo(targetEmail);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Fetch the target user's details.
                if (snapshot.exists()) {
                    Map<String, User> data = snapshot.getValue(new GenericTypeIndicator<Map<String, User>>() {
                    });
                    Map.Entry<String, User> entry = data.entrySet().iterator().next();

                    String targetUid = entry.getKey();
                    User targetUser = entry.getValue();
                    // If the target user don't allow chat invites,
                    // or don't have a chat that is currently opened
                    // then invite the target user to the chat.
                    // The key for the new record in the target user is the chat id
                    if (targetUser.isInvitesAllowed() && ((targetUser.getChatId() == null || TextUtils.isEmpty(targetUser.getChatId())))) {
                        final DatabaseReference targetUserReference = mFirebaseDatabase.getReference().child(Constants.PATH_USERS)
                                .child(targetUid);

                        targetUserReference.child(Constants.PATH_INVITES_RECEIVED)
                                // mChatId (of the current user) is the key
                                .child(mChatId).setValue(mFirebaseUser.getEmail(), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                Toast.makeText(getActivity(), R.string.invitation_sent_success, Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                    else if (!targetUser.isInvitesAllowed())
                        Toast.makeText(getActivity(), R.string.not_accepting_invites, Toast.LENGTH_LONG).show();
                        // target user already participating in some chat
                    else
                        Toast.makeText(getActivity(), R.string.user_already_in_chat, Toast.LENGTH_LONG).show();

                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // TODO log
            }
        });
    }


    /**
     * remove the user from the chat
     */
    private void removeUserFromChat() {
        // One less user in the chat so decrement the total number of users in the chat.
        // TODO uncomment after you decide where to store users count
        //atomicDecrementUsersCount();


        // Clear chatID in the db
        mUserChatIdentifier.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    Toast.makeText(getActivity(), R.string.yout_left_chat, Toast.LENGTH_SHORT).show();

                    // Stop listening to messages in the hat since the user left the chat.
                    if (mAdapter != null) {
                        mAdapter.stopListening();
                        mAdapter = null;
                    }

                    mSendMessageButton.setEnabled(false);
                    mMessageEditText.setEnabled(false);
                    mMessageEditText.setText("");

                    // Clear chatID locally
                    mChatId = null;


                    // User now don't have a chat so listen to when the user will have a new chat.
                    if (mUserChatIdentifierListener == null) {
                        mUserChatIdentifierListener = setUserChatIdentifierListener();
                        mUserChatIdentifier.addValueEventListener(mUserChatIdentifierListener);
                    }
                }
                else {
                    Log.e(TAG, "Error occurred while trying to leave the chat " + error.toException());
                    Toast.makeText(getActivity(), R.string.error_leave_chat, Toast.LENGTH_LONG).show();
                }
                }
        });


        // Change layout to empty chat layout.
        onLayoutChange(true);



    }

    private void enableAbilityToGetInvites() {
        // Allow the user to get chat invites after he left the chat.
        mUserReference.child(Constants.PATH_INVITES_ALLOWED).setValue(true);
        // Listen to chat Invites.
        if (mInvitesListener == null){
            mInvitesListener = setInvitesListener();
            mUserReference.child(Constants.PATH_INVITES_RECEIVED).addChildEventListener(mInvitesListener);
        }
    }

    /**
     * Change the chats fragment layout depending on whether
     * the user has an open chat
     *
     * @param shouldChangeToEmptyChat is true if the user don't have an open chat, false otherwise.
     */

    private void onLayoutChange(boolean shouldChangeToEmptyChat) {
/*
        // Default visibility values.
        int emptyChatVisibility = View.GONE;
        int chatVisibility = View.VISIBLE;
        // Change visibility default values.
        if (shouldChangeToEmptyChat) {
            emptyChatVisibility = View.VISIBLE;
            chatVisibility = View.GONE;

        }

        // Apply visibility on chat Views.
        mMessageEditText.setVisibility(chatVisibility);
        mSendMessageButton.setVisibility(chatVisibility);
        mRecyclerView.setVisibility(chatVisibility);
        mEmptyChatLayout.setVisibility(emptyChatVisibility);
*/
    }

    /**
     * Decrements atomically the number of users in the chat.
     * This method gets called when a user leaves the chat.
     */
    private void atomicDecrementUsersCount() {
        if (mChatId == null)
            return;

        final DatabaseReference userChatRef = mChatsReference.child(mChatId);
        userChatRef.child(Constants.PATH_CHAT_USERS_COUNT)
                .runTransaction(new Transaction.Handler() {
                    Integer usersCount;
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        // Get the number of users that the chat contains.
                        // This will be the default value for the users count.
                        usersCount = currentData.getValue(Integer.class);

                        if (usersCount == null)
                            return Transaction.success(currentData);
                        // Decrement the number of users and update the db.
                        usersCount--;
                        currentData.setValue(usersCount);
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                        // if the chat contains no users then delete it
                        if (usersCount < Constants.NEW_CHAT_USERS_COUNT)
                            userChatRef.removeValue();
                        Log.d(TAG, "postTransaction:onComplete:" + error);
                    }
                });
    }

    /**
     * add a new chat to the database and set the chatId of the user
     * to the key of the new chat.
     */
    private void addNewChatToDatabase() {
        // Create a new record for a message in the chat
        // and get the key of this record.
        final String newChatKey = mFirebaseDatabase.getReference()
                .child(Constants.PATH_CHATS)
                .push()
                .getKey();

        // Set the user's chat ID to the key we got previously.
        mUserReference.child(Constants.PATH_CHAT_ID)
                .setValue(newChatKey)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity()
                                , R.string.chat_open_success, Toast.LENGTH_SHORT)
                                .show();

                        // TODO change this to increment and not initialization to 1
                        // TODO uncomment after you decide where to store users count
                        //initUsersCountNodeInChat(newChatKey);


                        onLayoutChange(false);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity()
                        , R.string.error_open_chat, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    /**
     * Disable the user ability to get chat invites after the user opened a chat.
     */
    private void disableAbilityToGetInvites() {

        mIsAllowedToGetInvites = false;
        // TODO add completion listener
        mUserReference.child(Constants.PATH_INVITES_ALLOWED).setValue(false);
        if (mInvitesListener != null) {
            mUserReference.child(Constants.PATH_INVITES_RECEIVED).removeEventListener(mInvitesListener);
            mInvitesListener = null;
        }
    }



    //this method should insert the db a record that represents the number of users in the chat
    // using the chatKey
    // TODO decide where you want to store the users count
    private void initUsersCountNodeInChat(String chatKey) {
        mChatsReference.child(chatKey)
                .child(Constants.PATH_CHAT_USERS_COUNT)
                .setValue(Constants.NEW_CHAT_USERS_COUNT);
    }


    /**
     * add the user to a chat after he got invitation from another user
     *
     * @param chatId The id of the chat that the user was invited to.
     */
    // TODO implement a more efficient way
    private void addUserToChat(final String chatId) {

        mUserReference.child(Constants.PATH_CHAT_ID).setValue(chatId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(getActivity(), R.string.joined_chat_success, Toast.LENGTH_SHORT).show();

                        // TODO uncomment after you decide where to store users count
                       // atomicIncrementUsersCount();

                        // Remove all the invites since the user now have a chat.
                        mUserReference.child(Constants.PATH_INVITES_RECEIVED).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                // TODO implement behavior
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), R.string.error_cannot_join_chat, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void atomicIncrementUsersCount() {
        DatabaseReference userChatReference = mChatsReference.child(mChatId)
                .child(Constants.PATH_CHAT_USERS_COUNT);
        userChatReference.runTransaction(new Transaction.Handler() {
            Integer usersCount;
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                // Get the number of users that the chat contains.
                // This will be the default value for the users count.
                usersCount = currentData.getValue(Integer.class);

                if (usersCount == null)
                    return Transaction.success(currentData);
                // Decrement the number of users and update the db.
                usersCount++;
                currentData.setValue(usersCount);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                // TODO you got a new users count in the chat so update UI
                Log.d(TAG, "postTransaction:onComplete:" + error);
            }
        });
    }

    private void handleMessageDeploy() {
        // Click on the send button sends a message and clears the EditText
        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the current date and time with the default local of the user
                // TODO use Local and parsing
                Calendar calendar = Calendar.getInstance();
                Date currentDate = new Date();
                DateFormat dateFormat = SimpleDateFormat.getDateInstance();
                String sDate = dateFormat.format(calendar.getTime());
                dateFormat = SimpleDateFormat.getTimeInstance();
                String sTime = dateFormat.format(calendar.getTime());

                // Create a Message POJO for serialization
                Message message = new Message(
                        mMessageEditText.getText().toString(),
                        mFirebaseUser.getDisplayName(),
                        System.currentTimeMillis(),
                        mFirebaseUser.getUid());


                // Add a new message record to the user's chat
                mUserGroupChatReference.push().setValue(message);
                // Clear input box
                mMessageEditText.setText("");
            }
        });
    }

    private void handleMessageEditText() {
        mMessageEditText.setEnabled(true);
        mSendMessageButton.setEnabled(false);
        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // if the current text is not empty then allow the user to
                // send the message
                if (0 < s.toString().trim().length())
                    mSendMessageButton.setEnabled(true);
                else
                    mSendMessageButton.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});
    }



}