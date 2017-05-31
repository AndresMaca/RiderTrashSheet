package com.macapps.developer.ridertrash;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

/**
 * Created by Developer on 30/5/2017.
 */

public class CardFragment extends Fragment {

    Bus bus;
    private CardView cardView;
    public void setName(String name){

    }
    public interface onSomeEventListener {
        public void someEvent(String s);
    }
    onSomeEventListener someEventListener;
    public static Fragment getInstance(int position, Bus bus) {
        CardFragment f = new CardFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putParcelable("bus",bus);
        //  args.putString("raro","raro");
        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        try {
            someEventListener = (onSomeEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    @SuppressLint("DefaultLocale")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final Bundle bundle=getArguments();
        View view = inflater.inflate(R.layout.item_viewpager, container, false);

        cardView = (CardView) view.findViewById(R.id.cardView);
        cardView.setMaxCardElevation(cardView.getCardElevation() * CardAdapter.MAX_ELEVATION_FACTOR);

        final CircularProgressBar cpbTiempo = (CircularProgressBar) view.findViewById(R.id.cpbTiempo);
        final TextView title = (TextView) view.findViewById(R.id.title);
        TextView recorrido = (TextView) view.findViewById(R.id.recorrido);
        ImageView imageTipo = (ImageView) view.findViewById(R.id.imageTipo);
       // title.setText(String.format("Ruta %d", getArguments().getInt("position")));
        bus =bundle.getParcelable("bus");
        title.setText(bus.getRoute());

        recorrido.setText("Inicio: "+bus.getParada_inicial()+" Fin: "+ bus.getParada_final());
        imageTipo.setImageResource(bus.getImagen());
        cpbTiempo.setProgressWithAnimation(bus.getTiempo(), 1500);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cpbTiempo.setProgressWithAnimation(0, 1500);

                //     Toast.makeText(getActivity(), "Ruta " + getArguments().getInt("position") + " seleccionada!", Toast.LENGTH_SHORT).show();


                someEventListener.someEvent(bus.getParada_final());



            }
        });

        return view;
    }

    public CardView getCardView() {
        return cardView;
    }
}
