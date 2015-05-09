/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

package tm.veriloft.music.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import tm.veriloft.music.R;
import tm.veriloft.music.model.Audio;
import tm.veriloft.music.ui.MainActivity;


/**
 * Created by alashov on 08/12/14.
 */
public class AudioListAdapter extends BaseAdapter {

    private ArrayList<Audio> audioList;
    private Context context;
    private LayoutInflater inflater;

    /**
     * @param audioList list of array
     */
    public AudioListAdapter( Context _context, ArrayList<Audio> audioList ) {
        this.audioList = audioList;
        if (_context != null) {
            this.context = _context;
            try {
                this.inflater = LayoutInflater.from(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getCount() {
        return audioList.size();
    }

    @Override
    public Audio getItem( int position ) {
        return audioList.get(position);
    }

    @Override
    public long getItemId( int position ) {
        return position;
    }

    @Override
    public View getView( final int position, View convertView, ViewGroup parent ) {
        ViewHolder viewHolder;
        final Audio audio = audioList.get(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_audio, null);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.playButton = convertView.findViewById(R.id.play);
            viewHolder.duration = (Button) convertView.findViewById(R.id.duration);
            convertView.setTag(viewHolder);
        } else viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.name.setText(audio.getArtist() + " - " + audio.getTitle());
        viewHolder.duration.setText(secondsToString(audio.getDuration()));

        viewHolder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick( View v ) {
                ((MainActivity)context).playAudio(Uri.parse(audio.getSrc()));
            }
        });

        return convertView;
    }

    public class ViewHolder {
        TextView name;
        Button duration;
        View playButton;
    }

    /**
     *
     * @param seconds seconds to format
     * @return converted to mm:ss duration
     */
    private String secondsToString( int seconds ) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
}
