package name.zenochan.stickheaderdecoration.demo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import kale.adapter.CommonRcvAdapter;
import kale.adapter.item.AdapterItem;
import name.zenochan.stickyheader.SideBar;
import name.zenochan.stickyheader.StickyHeaderDecoration;

public class MainActivity extends AppCompatActivity
{

  @BindView(R.id.toolbar)     Toolbar              toolbar;
  @BindView(R.id.rcv_content) RecyclerView         rcvContent;
  @BindView(R.id.fab)         FloatingActionButton fab;
  @BindView(R.id.right_side)  SideBar              rightSide;

  private List<String>             items;
  private CommonRcvAdapter<String> adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(view -> {
      randomString();
      adapter.setData(items);
      adapter.notifyDataSetChanged();
    });

    init(savedInstanceState);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    int id = item.getItemId();

    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void init(Bundle savedInstanceState)
  {
    randomString();
    rcvContent.setLayoutManager(new LinearLayoutManager(this));
    adapter = new CommonRcvAdapter<String>(items)
    {
      @NonNull @Override public AdapterItem createItem(Object o)
      {
        return new StringItem();
      }
    };
    rcvContent.setAdapter(adapter);
    rcvContent.addItemDecoration(new StickyHeaderDecoration(position -> items.get(position).toUpperCase().substring(0, 1)));
    rightSide.setupWithRecycler(rcvContent, position -> {
      if (items.size() > 0 && items.size() > position) {
        return items.get(position).substring(0, 1).toUpperCase();
      } else {
        return "";
      }
    });
  }

  private void randomString()
  {
    Random random = new Random();
    items = new ArrayList<>();
    for (int i = 0; i < 48 + random.nextInt(48); i++) {
      items.add(getChines());
    }
    Collections.sort(items, (o1, o2) -> o1.substring(0, 1).compareTo(o2.substring(0, 1)));
  }

  private static String getChines()
  {
    Random random  = new Random();
    String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String str     = "";
    for (int i = 0; i < 1 + random.nextInt(16); i++) {
      int p = random.nextInt(letters.length() - 1);
      str += letters.substring(p, p + 1);
    }
    return str;
  }

  public static String getChinese()
  {
    String str    = null;
    int    highPos, lowPos;
    Random random = new Random();
    highPos = (176 + Math.abs(random.nextInt(71)));//区码，0xA0打头，从第16区开始，即0xB0=11*16=176,16~55一级汉字，56~87二级汉字
    random = new Random();
    lowPos = 161 + Math.abs(random.nextInt(94));//位码，0xA0打头，范围第1~94列

    byte[] bArr = new byte[2];
    bArr[0] = (Integer.valueOf(highPos)).byteValue();
    bArr[1] = (Integer.valueOf(lowPos)).byteValue();
    try {
      str = new String(bArr, "GB2312");   //区位码组合成汉字
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return str;
  }

}
