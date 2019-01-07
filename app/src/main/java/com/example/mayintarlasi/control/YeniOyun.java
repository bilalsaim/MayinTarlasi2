package com.example.mayintarlasi.control;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mayintarlasi.R;
import com.example.mayintarlasi.model.Kare;
import com.example.mayintarlasi.model.Secenek;

import org.apache.commons.lang3.StringUtils;

import java.util.Random;

@RequiresApi(api = Build.VERSION_CODES.N)
public class YeniOyun extends Activity {

    private ImageButton btnBaslat;
    private TextView textMayinSayisi;
    private TextView textZaman;
    private MediaPlayer sesKaybetme;
    private MediaPlayer sesKazanma;
    private Kare kareler[][];
    private Handler zaman = new Handler();
    private int gecenSure = 0;
    private boolean isZamanBasladi;
    private boolean isMayinlarYerlestirildi;
    private boolean isOyunBitti;
    private int kalanMayin; //Bulunmamış mayın sayısı
    private Secenek secenek;

    //TODO: Değeri salladım düzelt
    private int kareBoyutu = 128; // Blok yükseklikleri
    private int kareBoslugu = 2; // Kareler arası boşluklar
    private TableLayout mayinAlani;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.yenioyunl);

        secenek = Secenek.getInstance();

        mayinAlani = (TableLayout)findViewById(R.id.MayinAlani);
		textMayinSayisi = (TextView) findViewById(R.id.yoTextMayinSayisi);
		textZaman = (TextView) findViewById(R.id.yoTextSure);

		sesKaybetme = MediaPlayer.create(this, R.raw.bomb);
		sesKazanma = MediaPlayer.create(this, R.raw.alkis);

		showDialog("Oyunu başlatmak için surata tıklatınız!", 5000, true, false);

        btnBaslat = (ImageButton) findViewById(R.id.yoBtnBaslat);
        btnBaslat.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                oyunuBaslat();
            }
        });

        oyunuBaslat();
	}

    private void oyunuSifirla()
    {
        zamaniDurdur();
        textZaman.setText("000");
        textMayinSayisi.setText("000");
        btnBaslat.setBackgroundResource(R.drawable.smile);
        mayinAlani.removeAllViews();

        isZamanBasladi = false;
        isMayinlarYerlestirildi = false;
        isOyunBitti = false;
        kalanMayin = 0;
    }

    public void zamanaiBaslat()
    {
        if (gecenSure == 0)
        {
            zaman.removeCallbacks(updateTimeElasped);
            zaman.postDelayed(updateTimeElasped, 1000);
        }
    }

    public void zamaniDurdur()
    {
        zaman.removeCallbacks(updateTimeElasped);
    }

	private void oyunuBaslat()
	{
        oyunuSifirla();
		mayinAlaniniOlustur();
		mayinAlaniniGoster();

		kalanMayin = secenek.getMayin();
		isOyunBitti = false;
		gecenSure = 0;
	}

    private void mayinAlaniniOlustur()
    {
        kareler = new Kare[secenek.getX() + 2][secenek.getY() + 2];

        for (int satir = 0; satir < secenek.getX() + 2; satir++)
        {
            for (int sutun = 0; sutun < secenek.getY() + 2; sutun++)
            {
                kareler[satir][sutun] = new Kare(this);
                kareler[satir][sutun].varsayilanAyarlar();

                final int gecerliSatir = satir;
                final int gecerliSutun = sutun;

                kareler[satir][sutun].setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(isOyunBitti) return;

                        if (!isZamanBasladi)
                        {
                            zamanaiBaslat();
                            isZamanBasladi = true;
                        }
                        
                        if (!isMayinlarYerlestirildi)
                        {
                            isMayinlarYerlestirildi = true;
                            mayinlariYerlestir(gecerliSatir, gecerliSutun);
                        }

                        if (!kareler[gecerliSatir][gecerliSutun].isBayrak())
                        {
                            kareAc(gecerliSatir, gecerliSutun);

                            //Mayına basılıysa oyunu kaybet
                            if (kareler[gecerliSatir][gecerliSutun].isMayin())
                            {
                                oyunKaybedildi(gecerliSatir,gecerliSutun);
                            }
                            
                            if (kazanmaKontrol()) oyunKazanildi();
                        }
                    }
                });

                kareler[satir][sutun].setOnLongClickListener(new OnLongClickListener()
                {
                    public boolean onLongClick(View view)
                    {
                        if(isOyunBitti || !kareler[gecerliSatir][gecerliSutun].isKapali()) return true;

                        if(secenek.isSoruIsareti())
                        {
                            if (kareler[gecerliSatir][gecerliSutun].isClickable() &&
                                    (kareler[gecerliSatir][gecerliSutun].isEnabled() || kareler[gecerliSatir][gecerliSutun].isBayrak()))
                            {
                                if (!kareler[gecerliSatir][gecerliSutun].isBayrak() && !kareler[gecerliSatir][gecerliSutun].isSoruIsareti())
                                {
                                    kareler[gecerliSatir][gecerliSutun].setDisabledKare(false);
                                    kareler[gecerliSatir][gecerliSutun].setBayrakSimgesi(true);
                                    kareler[gecerliSatir][gecerliSutun].setBayrak(true);
                                    kalanMayin--;
                                    mayinSayisiGostergesiniGuncelle();
                                }
                                else if (!kareler[gecerliSatir][gecerliSutun].isSoruIsareti())
                                {
                                    kareler[gecerliSatir][gecerliSutun].setDisabledKare(true);
                                    kareler[gecerliSatir][gecerliSutun].setSoruIsaretiSimgesi(true);
                                    kareler[gecerliSatir][gecerliSutun].setBayrak(false);
                                    kareler[gecerliSatir][gecerliSutun].setSoruIsareti(true);
                                    kalanMayin++;
                                    mayinSayisiGostergesiniGuncelle();
                                }
                                else
                                {
                                    kareler[gecerliSatir][gecerliSutun].setDisabledKare(true);
                                    kareler[gecerliSatir][gecerliSutun].tumSimgeleriTemizle();
                                    kareler[gecerliSatir][gecerliSutun].setSoruIsareti(false);

                                    if (kareler[gecerliSatir][gecerliSutun].isBayrak())
                                    {
                                        kalanMayin++;
                                        mayinSayisiGostergesiniGuncelle();
                                    }

                                    kareler[gecerliSatir][gecerliSutun].setBayrak(false);
                                }

                                mayinSayisiGostergesiniGuncelle();
                            }
                        }
                        return true;
                    }
                });
            }
        }
    }

    private void mayinlariYerlestir(int gecerliSatir, int gecerliSutun)
    {
        Random rand = new Random();
        int mayinSatir, mayinSutun;

        int satir = 0;
        while(satir < secenek.getMayin()){
            mayinSatir = rand.nextInt(secenek.getY());
            mayinSutun = rand.nextInt(secenek.getX());
            if ((mayinSatir + 1 != gecerliSutun) || (mayinSutun + 1 != gecerliSatir))
            {
                if (!kareler[mayinSutun + 1][mayinSatir + 1].isMayin())
                {
                    kareler[mayinSutun + 1][mayinSatir + 1].mayinEkle();
                    satir++;
                }
            }
        }

        int komsuMayinSayisi;
        for (satir = 0; satir < secenek.getX() + 2; satir++)
        {
            for (int sutun = 0; sutun < secenek.getY() + 2; sutun++)
            {
                komsuMayinSayisi = 0;
                if ((satir != 0) && (satir != (secenek.getX() + 1)) && (sutun != 0) && (sutun != (secenek.getY() + 1)))
                {
                    for (int ilerikiSatir = -1; ilerikiSatir < 2; ilerikiSatir++)
                    {
                        for (int ilerikiSutun = -1; ilerikiSutun < 2; ilerikiSutun++)
                        {
                            if (kareler[satir + ilerikiSatir][sutun + ilerikiSutun].isMayin())
                            {
                                komsuMayinSayisi++;
                            }
                        }
                    }

                    kareler[satir][sutun].setKomsuMayinSayisi(komsuMayinSayisi);
                }
                else
                {
                    kareler[satir][sutun].setKomsuMayinSayisi(9);
                    kareler[satir][sutun].kareAc();
                }
            }
        }
    }

    private void mayinAlaniniGoster()
	{
		for (int satir = 1; satir < secenek.getX() + 1; satir++)
		{
			TableRow tableRow = new TableRow(this);
			tableRow.setLayoutParams(new LayoutParams((kareBoyutu + 2 * kareBoslugu) * secenek.getY(), kareBoyutu + 2 * kareBoslugu));

			for (int sutun = 1; sutun < secenek.getY() + 1; sutun++)
			{
				kareler[satir][sutun].setLayoutParams(new LayoutParams(kareBoyutu + 2 * kareBoslugu, kareBoyutu + 2 * kareBoslugu));
				kareler[satir][sutun].setPadding(kareBoslugu, kareBoslugu, kareBoslugu, kareBoslugu);
				tableRow.addView(kareler[satir][sutun]);
			}
			mayinAlani.addView(tableRow,new TableLayout.LayoutParams(
					(kareBoyutu + 2 * kareBoslugu) * secenek.getY(), kareBoyutu + 2 * kareBoslugu));
		}
	}

	private boolean kazanmaKontrol()
	{
		for (int satir = 1; satir < secenek.getX() + 1; satir++)
		{
			for (int sutun = 1; sutun < secenek.getY() + 1; sutun++)
			{
				if (!kareler[satir][sutun].isMayin() && kareler[satir][sutun].isKapali()) return false;
			}
		}
		return true;
	}

	private void mayinSayisiGostergesiniGuncelle()
	{
        String onEk = (gecenSure < 100)? StringUtils.repeat("0", 3-String.valueOf(kalanMayin).length()) : "";
		textMayinSayisi.setText(onEk + Integer.toString(kalanMayin));
	}

	private void oyunKazanildi()
	{
		zamaniDurdur();
		isZamanBasladi = false;
		isOyunBitti = true;
		kalanMayin = 0;

		btnBaslat.setBackgroundResource(R.drawable.cool);
		mayinSayisiGostergesiniGuncelle();

		for (int satir = 1; satir < secenek.getX() + 1; satir++)
		{
			for (int sutun = 1; sutun < secenek.getY() + 1; sutun++)
			{
				kareler[satir][sutun].setClickable(false);
				if (kareler[satir][sutun].isMayin())
				{
					kareler[satir][sutun].setDisabledKare(false);
					kareler[satir][sutun].setBayrakSimgesi(true);
				}
			}
		}

		if(secenek.isSes()) sesKazanma.start();
        
		showDialog("Kazandın " + Integer.toString(gecenSure) + " saniyede!", 1000, false, true);
	}

	private void oyunKaybedildi(int gecerliSatir, int gecerliSutun)
	{
		isOyunBitti = true;
		zamaniDurdur();
		isZamanBasladi = false;
		btnBaslat.setBackgroundResource(R.drawable.sad);

		for (int satir = 1; satir < secenek.getX() + 1; satir++)
		{
			for (int sutun = 1; sutun < secenek.getY() + 1; sutun++)
			{
				kareler[satir][sutun].setDisabledKare(false);
                kareler[satir][sutun].setClickable(false);

				if (kareler[satir][sutun].isMayin() && !kareler[satir][sutun].isBayrak())
				{
					kareler[satir][sutun].setMineIcon(false);
				}

				if (!kareler[satir][sutun].isMayin() && kareler[satir][sutun].isBayrak())
				{
					kareler[satir][sutun].setBayrakSimgesi(false);
				}
			}
		}

		kareler[gecerliSatir][gecerliSutun].triggerMine();
		if(secenek.isSes()) sesKaybetme.start();

		showDialog("Tekrar Dene " + Integer.toString(gecenSure) + " saniye!", 1000, false, false);
	}

	private void kareAc(int tiklananSatir, int tiklananSutun)
	{
		if (kareler[tiklananSatir][tiklananSutun].isMayin() || kareler[tiklananSatir][tiklananSutun].isBayrak()) return;

		kareler[tiklananSatir][tiklananSutun].kareAc();

		if (kareler[tiklananSatir][tiklananSutun].getKomsuMayinSayisi() != 0 ) return;

		for (int satir = 0; satir < 3; satir++)
		{
			for (int sutun = 0; sutun < 3; sutun++)
			{
				if (kareler[tiklananSatir + satir - 1][tiklananSutun + sutun - 1].isKapali()
						&& (tiklananSatir + satir - 1 > 0) && (tiklananSutun + sutun - 1 > 0)
						&& (tiklananSatir + satir - 1 < secenek.getX() + 1) && (tiklananSutun + sutun - 1 < secenek.getY() + 1))
				{
					kareAc(tiklananSatir + satir - 1, tiklananSutun + sutun - 1 );
				}
			}
		}
		return;
	}

	private Runnable updateTimeElasped = new Runnable()
	{
		public void run()
		{
			long currentMilliseconds = System.currentTimeMillis();
			++gecenSure;

			String onEk = (gecenSure < 100)? StringUtils.repeat("0", 3-String.valueOf(gecenSure).length()) : "";
			textZaman.setText(onEk + Integer.toString(gecenSure));

			zaman.postAtTime(this, currentMilliseconds);
			zaman.postDelayed(updateTimeElasped, 1000);
		}
	};

	private void showDialog(String message, int milliseconds, boolean isBaslatResim, boolean isKazanildiResim)
	{
		Toast dialog = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
		dialog.setGravity(Gravity.CENTER, 0, 0);
		LinearLayout dialogView = (LinearLayout) dialog.getView();
		ImageView uyariResim = new ImageView(getApplicationContext());

		if (isBaslatResim) uyariResim.setImageResource(R.drawable.smile);
		else if (isKazanildiResim) uyariResim.setImageResource(R.drawable.cool);
		else uyariResim.setImageResource(R.drawable.sad);

		dialogView.addView(uyariResim, 0);
		dialog.setDuration(milliseconds);
		dialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.yeni_oyun, menu);
		return true;
	}

}