package name.zenochan.stickyheader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import lombok.Setter;


/**
 * <li>{@link #setupWithRecycler(RecyclerView, IndexProvider)} 与recyclerView 协同工作</li>
 * <li>{@link #setTextColor(int)} 设置字体颜色</li>
 * <li>{@link #setTextColorAccent(int)} 设置字体高亮颜色</li>
 * <li>{@link #setPressBackgroundColor(int)} press 时的背景色</li>
 *
 * @author 陈治谋 (513500085@qq.com)
 * @since 2016-08-16
 */
public class SideBar extends View
{
  private SimpleArrayMap<Integer, String> indexMap = new SimpleArrayMap<>();
  private RecyclerView recyclerView;
  private int   choose   = -1;// 选中
  private int   position = -1;
  private Paint paint    = new Paint();

  private int offsetY;
  private int singleHeight;

  private IndexProvider provider;

  @ColorInt @Setter(onMethod = @__({@ColorInt}))
  private int textColor            = Color.parseColor("#424242");
  @ColorInt @Setter(onMethod = @__({@ColorInt}))
  private int textColorAccent      = Color.parseColor("#ff4081");
  @ColorInt @Setter(onMethod = @__({@ColorInt}))
  private int pressBackgroundColor = Color.parseColor("#66000000");

  public SideBar(Context context)
  {
    this(context, null);
  }

  public SideBar(Context context, AttributeSet attrs)
  {
    this(context, attrs, 0);
  }


  public SideBar(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);
  }

  @Override protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    ((ViewGroup) getParent()).setClipChildren(false);
  }

  public void setupWithRecycler(@NonNull RecyclerView recyclerView, @NonNull IndexProvider provider)
  {
    this.recyclerView = recyclerView;
    this.provider = provider;

    this.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
    {
      @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy)
      {
        super.onScrolled(recyclerView, dx, dy);
        int                        position      = -1;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
          position = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        if (position != -1 && position != SideBar.this.position) {
          SideBar.this.position = position;
          invalidate();
        }
      }
    });
    final RecyclerView.Adapter adapter = recyclerView.getAdapter();
    if (adapter == null) {
      throw new IllegalArgumentException("recyclerView do not set adapter");
    }
    adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver()
    {
      @Override public void onChanged()
      {
        super.onChanged();
        initIndex(adapter);
      }

      @Override public void onItemRangeChanged(int positionStart, int itemCount)
      {
        super.onItemRangeChanged(positionStart, itemCount);
        initIndex(adapter);
      }

    });
    initIndex(adapter);
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent event)
  {
    final int   action    = event.getAction();
    final float y         = event.getY();// 点击y坐标
    final int   oldChoose = choose;
    // 点击y坐标所占总高度的比例*b数组的长度就等于点击b中的个数.
    final int c = (int) ((y - offsetY) / singleHeight);

    switch (action) {
      case MotionEvent.ACTION_UP:
        setBackgroundColor(Color.TRANSPARENT);
        choose = -1;
        invalidate();
        break;

      default:
        setBackgroundColor(pressBackgroundColor);
        if (oldChoose != c) {
          if (c >= 0 && c < indexMap.size()) {
            int position = indexMap.keyAt(c);
            recyclerView.getLayoutManager().scrollToPosition(position);
            choose = c;
            invalidate();
          }
        }

        break;
    }
    return true;
  }

  protected void onDraw(Canvas canvas)
  {
    super.onDraw(canvas);

    if (indexMap.isEmpty()) return;

    int height = getHeight(); // 获取对应高度
    int width  = getWidth();   // 获取对应宽度

    singleHeight = height / indexMap.size();// 获取每一个字母的高度
    int dp12 = dip2px(12);
    int dp24 = dip2px(24);
    singleHeight = singleHeight > dp24 ? dp24 : singleHeight;
    offsetY = (height - singleHeight * indexMap.size()) / 2;

    // 绘制字母
    for (int i = 0; i < indexMap.size(); i++) {
      int key     = indexMap.keyAt(i);
      int nextKey = key + 1;
      if (indexMap.size() > i + 1) {
        nextKey = indexMap.keyAt(i + 1);
      }

      paint.setAntiAlias(true);
      paint.setTextSize(dp12);
      paint.setTypeface(Typeface.DEFAULT);

      int color = choose == -1 && position >= key && position < nextKey || i == choose
          ? textColorAccent : textColor;
      paint.setColor(color);

      float xPos = width / 2 - paint.measureText(indexMap.get(key)) / 2;
      float yPos = offsetY + singleHeight * (i + 0.5F);
      if (i == choose) {
        // 选中的状态
        paint.setFakeBoldText(true);
        paint.setTextSize(dp24);
        canvas.drawText(indexMap.get(indexMap.keyAt(i)), dip2px(-56), yPos, paint);
        paint.setTextSize(dp12);
      }
      // x坐标等于中间-字符串宽度的一半.
      canvas.drawText(indexMap.get(indexMap.keyAt(i)), xPos, yPos, paint);
    }

  }

  private void initIndex(@NonNull RecyclerView.Adapter adapter)
  {
    indexMap.clear();
    for (int i = 0; i < adapter.getItemCount(); i++) {
      String item = provider.get(i);
      if (i == 0) {
        indexMap.put(i, item);
      } else {
        String preItem = provider.get(i - 1);
        if (preItem != null && !preItem.equals(item)) {
          indexMap.put(i, item);
        }
      }
    }

    //重绘
    invalidate();
  }

  private static int dip2px(float dpValue)
  {
    final float scale = Resources.getSystem().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }

}
