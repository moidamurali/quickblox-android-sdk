package com.quickblox.sample.chat.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.TimeUtils;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.chat.utils.qb.QbUsersHolder;
import com.quickblox.sample.core.utils.ResourceUtils;
import com.quickblox.users.model.QBUser;

import java.util.Collection;
import java.util.List;

public class ChatAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private List<QBChatMessage> chatMessages;

    public ChatAdapter(Context context, List<QBChatMessage> chatMessages) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.chatMessages = chatMessages;
    }

    @Override
    public int getCount() {
        return chatMessages.size();
    }

    @Override
    public QBChatMessage getItem(int position) {
        return chatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void add(QBChatMessage message) {
        chatMessages.add(message);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_item_chat_message, parent, false);

            holder.messageBodyTextView = (TextView) convertView.findViewById(R.id.text_image_message);
            holder.messageAuthorTextView = (TextView) convertView.findViewById(R.id.text_message_author);
            holder.messageContainerLayout = (LinearLayout) convertView.findViewById(R.id.layout_chat_message_container);
            holder.textContainerLayout = (LinearLayout) convertView.findViewById(R.id.layout_message_content_container);
            holder.messageInfoTextView = (TextView) convertView.findViewById(R.id.text_message_info);
            holder.attachmentImageView = (ImageView) convertView.findViewById(R.id.image_message_attachment);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        QBChatMessage chatMessage = getItem(position);
        QBUser currentUser = ChatHelper.getCurrentUser();
        boolean isIncomingMessage = chatMessage.getSenderId() != null && !chatMessage.getSenderId().equals(currentUser.getId());

        setIncomingOrOutgoingMessageAttributes(holder, isIncomingMessage);
        setMessageBody(holder, chatMessage);
        setMessageInfo(chatMessage, holder);
        setMessageAuthor(holder, chatMessage, isIncomingMessage);

        holder.messageContainerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isMessageInfoVisible = holder.messageInfoTextView.getVisibility() == View.VISIBLE;
                holder.messageInfoTextView.setVisibility(isMessageInfoVisible ? View.GONE : View.VISIBLE);
            }
        });
        holder.messageInfoTextView.setVisibility(View.GONE);

        return convertView;
    }

    private void setMessageBody(ViewHolder holder, QBChatMessage chatMessage) {
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        if (attachments == null || attachments.isEmpty()) {
            holder.messageBodyTextView.setText(chatMessage.getBody());
            holder.messageBodyTextView.setVisibility(View.VISIBLE);
            holder.attachmentImageView.setVisibility(View.GONE);
        } else {
            QBAttachment attachment = attachments.iterator().next();

            holder.messageBodyTextView.setVisibility(View.GONE);
            holder.attachmentImageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(attachment.getUrl())
                    .into(holder.attachmentImageView);
        }
    }

    private void setMessageAuthor(ViewHolder holder, QBChatMessage chatMessage, boolean isIncomingMessage) {
        if (isIncomingMessage) {
            QBUser sender = QbUsersHolder.getInstance().getUserById(chatMessage.getSenderId());
            holder.messageAuthorTextView.setText(sender.getFullName());
            holder.messageAuthorTextView.setVisibility(View.VISIBLE);
        } else {
            holder.messageAuthorTextView.setVisibility(View.GONE);
        }
    }

    private void setMessageInfo(QBChatMessage chatMessage, ViewHolder holder) {
        holder.messageInfoTextView.setText(TimeUtils.getTime(chatMessage.getDateSent() * 1000));
    }

    @SuppressLint("RtlHardcoded")
    private void setIncomingOrOutgoingMessageAttributes(ViewHolder holder, boolean isIncoming) {
        int gravity = isIncoming ? Gravity.LEFT : Gravity.RIGHT;
        holder.messageContainerLayout.setGravity(gravity);
        holder.messageInfoTextView.setGravity(gravity);

        int textContainerBgResource = isIncoming
                ? R.drawable.incoming_message_bg
                : R.drawable.outgoing_message_bg;
        holder.textContainerLayout.setBackgroundResource(textContainerBgResource);

        int textColorResource = isIncoming
                ? R.color.text_color_black
                : R.color.text_color_white;
        holder.messageBodyTextView.setTextColor(ResourceUtils.getColor(textColorResource));
    }

    private static class ViewHolder {
        public TextView messageBodyTextView;
        public TextView messageAuthorTextView;
        public TextView messageInfoTextView;
        public LinearLayout messageContainerLayout;
        public LinearLayout textContainerLayout;
        public ImageView attachmentImageView;
    }
}
