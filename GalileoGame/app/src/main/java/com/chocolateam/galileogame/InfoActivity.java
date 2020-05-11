package com.chocolateam.galileogame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class InfoActivity extends AppCompatActivity {

    private Button whatIsGal;
    private Button canUseGal;
    private Button canUseRawM;
    private Button whatIsESA;
    private Button whatIsGSA;

    private List<Button> allButtons;

    private TextView infoText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_info);

        whatIsGal = findViewById(R.id.info_whatIsGal);
        canUseGal = findViewById(R.id.info_canUseGal);
        canUseRawM = findViewById(R.id.info_canUseRawM);
        whatIsESA = findViewById(R.id.info_whatIsESA);
        whatIsGSA = findViewById(R.id.info_whatIsGSA);

        allButtons = new ArrayList<Button>();
        allButtons.add(whatIsGal);
        allButtons.add(canUseGal);
        allButtons.add(canUseRawM);
        allButtons.add(whatIsESA);
        allButtons.add(whatIsGSA);

        infoText = findViewById(R.id.info_text);
        infoText.setMovementMethod(LinkMovementMethod.getInstance());

        ImageView clouds = findViewById(R.id.clouds);

        Animation ViewAnimation = AnimationUtils.loadAnimation(this, R.anim.rotation_slow);
        clouds.startAnimation(ViewAnimation);
    }

    public void backToMenu(View view) {
        finish();
    }

    public void infoWhatIsGal(View view) {
        enableAllButtons();
        disableButton(whatIsGal);
        infoText.post(new Runnable() {
            @Override
            public void run() {
                infoText.setText(R.string.infoWhatIsGalText);
            }
        });
    }

    public void infoCanUseGal(View view) {
        enableAllButtons();
        disableButton(canUseGal);
        infoText.post(new Runnable() {
            @Override
            public void run() {
                infoText.setText(R.string.infoCanUseGalText);
            }
        });
    }

    public void infoCanUseRawM(View view) {
        enableAllButtons();
        disableButton(canUseRawM);
        infoText.post(new Runnable() {
            @Override
            public void run() {
                infoText.setText(R.string.infoCanUseRawMText);
            }
        });
    }

    public void infoESA(View view) {
        enableAllButtons();
        disableButton(whatIsESA);
        infoText.post(new Runnable() {
            @Override
            public void run() {
                infoText.setText(R.string.infoESAText);
            }
        });
    }

    public void infoGSA(View view) {
        enableAllButtons();
        disableButton(whatIsGSA);
        infoText.post(new Runnable() {
            @Override
            public void run() {
                infoText.setText(R.string.infoGSAText);
            }
        });
    }


    private void disableButton(Button button) {
        button.setEnabled(false);
        button.setBackgroundColor(getResources().getColor(R.color.ap_light_gray));
    }

    private void enableAllButtons() {
        for (Button button : allButtons) {
            button.setEnabled(true);
            button.setBackgroundColor(getResources().getColor(R.color.buttonGreen));
        }
    }
}
