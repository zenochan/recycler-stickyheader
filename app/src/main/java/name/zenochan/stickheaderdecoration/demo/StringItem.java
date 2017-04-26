package name.zenochan.stickheaderdecoration.demo;

import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import kale.adapter.item.AdapterItem;

/**
 * @author 陈治谋 (微信: puppet2436)
 * @since 2017/4/23
 */
public class StringItem implements AdapterItem<String>
{
  @BindView(R.id.tv_string) TextView tvString;

  @Override public int getLayoutResId()
  {
    return R.layout.item_string;
  }

  @Override public void bindViews(View view)
  {
    ButterKnife.bind(this,view);
  }

  @Override public void setViews()
  {

  }

  @Override public void handleData(String s, int i)
  {
    tvString.setText(s);
  }
}
