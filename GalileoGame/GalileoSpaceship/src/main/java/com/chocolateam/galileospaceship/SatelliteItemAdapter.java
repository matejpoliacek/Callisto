package com.chocolateam.galileospaceship;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chocolateam.galileospaceship.R;
import com.galfins.gnss_compare.Constellations.SatelliteParameters;

import java.util.List;

/**
 * Created by lgr on 06/01/2018.
 */

public class SatelliteItemAdapter extends RecyclerView.Adapter<SatelliteItemAdapter.MyViewHolder> {

    private final String TAG = this.getClass().getSimpleName();

    private List<SatelliteParameters> mSatellites;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView midView, mconstelationView;
        private ImageView mflagView;
        private ImageView msignalView;

        public MyViewHolder(View view) {
            super(view);
            midView = view.findViewById(R.id.id);
            mflagView = view.findViewById(R.id.flag);
            msignalView = view.findViewById(R.id.signal);
            mconstelationView = view.findViewById(R.id.constellation);
        }
    }

    public SatelliteItemAdapter(List<SatelliteParameters> satellites) {
        this.mSatellites = satellites;
    }

    public void setSatelliteList(List<SatelliteParameters> satellites) {
        this.mSatellites = satellites;
    }

    @Override
    public SatelliteItemAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sattelite_item_row, parent, false);

        return new SatelliteItemAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SatelliteParameters satellite = mSatellites.get(position);
        holder.midView.setText(String.format("%05d", satellite.getSatId()));
        holder.msignalView.setImageResource(getSignalBitmap((int) (6 * Math.min(satellite.getSignalStrength()/40, 1))));
        holder.mflagView.setImageResource(getFlagBitmap(satellite.getConstellationType()));
          Log.e(TAG, "getConstellationType()" + satellite.getConstellationType());
    }

    @Override
    public int getItemCount() {
        return mSatellites.size();
    }

    /**
     * Retrieve the flag Bitmap associated to flag_id flag
     */
    public int getFlagBitmap(int flag_id){

            switch (flag_id) {
                case 1:
                    return R.drawable.us_flag;
                case 2:
                    return R.drawable.ru_flag;
                case 3:
                    return R.drawable.ru_flag;
                case 4:
                    return R.drawable.ja_flag;
                case 5:
                    return R.drawable.ch_flag;
                case 6:
                    return R.drawable.eu_flag;
                default:
                    return R.drawable.unknown_flag;
            }
    }

    /**
      * Retrieve the signal Bitmap associated to signal_id signal level
      */
    public int getSignalBitmap(int signal_id){

        switch(signal_id){
            case 0:
                return R.drawable.signal_0;
            case 1:
                return R.drawable.signal_1;
            case 2:
                return R.drawable.signal_2;
            case 3 :
                return R.drawable.signal_3;
            case 4 :
                return R.drawable.signal_4;
            case 5 :
                return R.drawable.signal_5;
            case 6 :
                return R.drawable.signal_6;
            default:
                return R.drawable.signal_default;
        }

    }

}
