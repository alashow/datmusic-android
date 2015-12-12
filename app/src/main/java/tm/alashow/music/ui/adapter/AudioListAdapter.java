/*
 * Copyright 2015. Alashov Berkeli
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package tm.alashow.music.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import tm.alashow.music.R;
import tm.alashow.music.model.Audio;
import tm.alashow.music.ui.activity.MainActivity;

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
    public AudioListAdapter(Context _context, ArrayList<Audio> audioList) {
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
    public Audio getItem(int position) {
        return audioList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        final Audio audio = audioList.get(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_audio, null);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.playButton = convertView.findViewById(R.id.play);
            viewHolder.duration = (TextView) convertView.findViewById(R.id.duration);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.name.setText(String.format("%s - %s", audio.getArtist(), audio.getTitle()));
        viewHolder.duration.setText(secondsToString(audio.getDuration()));

        viewHolder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).playAudio(audio);
            }
        });

        return convertView;
    }

    public class ViewHolder {
        TextView name;
        TextView duration;
        View playButton;
    }

    /**
     * @param seconds seconds to format
     * @return converted to mm:ss duration
     */
    private String secondsToString(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
}
