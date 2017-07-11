package name.zenochan.stickyheader;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

/**
 * <li>{@link #setHeaderHeight(int)}  高度(dp)
 * <li>{@link #setHeaderColor(int)}   背景色(dp)
 * <li>{@link #setTextSize(int)}      文字大小(dp)
 * <li>{@link #setTextColor(int)}     文字颜色(dp)
 * <li>{@link #setPaddingLeft(int)}   左边距(dp)
 *
 * @author 陈治谋 (微信: puppet2436)
 * @see <a href="http://www.jianshu.com/p/b46a4ff7c10a">RecyclerView之ItemDecoration由浅入深</a>
 * @since 2017/4/23
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class StickyHeaderDecoration extends RecyclerView.ItemDecoration
{
  private IndexProvider provider;

  private Paint     paint;
  private TextPaint textPaint;

  private int headerHeight = dp2px(24);   // header 高度
  private int textSize     = dp2px(16);   // 字体大小

  private Paint.FontMetrics fontMetrics;

  @ColorInt private int headerColor = Color.parseColor("#ff4081");
  private           int paddingLeft = dp2px(16);

  public StickyHeaderDecoration(IndexProvider provider)
  {
    this.provider = provider;

    paint = new Paint();
    paint.setColor(headerColor);

    fontMetrics = new Paint.FontMetrics();
    textPaint = new TextPaint();
    textPaint.setTypeface(Typeface.DEFAULT_BOLD);
    textPaint.setAntiAlias(true);
    textPaint.setTextSize(textSize);
    textPaint.setColor(Color.WHITE);
    textPaint.getFontMetrics(fontMetrics);
    textPaint.setTextAlign(Paint.Align.LEFT);
  }

  public void setTextSize(int dp)
  {
    this.textSize = dp2px(dp);
    textPaint.setTextSize(this.textSize);
    textPaint.getFontMetrics(fontMetrics);
  }

  public void setTextColor(@ColorInt int color)
  {
    textPaint.setColor(color);
  }

  public void setHeaderColor(@ColorInt int headerColor)
  {
    this.headerColor = headerColor;
    paint.setColor(headerColor);
  }

  public void setHeaderHeight(int dp)
  {
    this.headerHeight = dp2px(dp);
  }

  public void setPaddingLeft(int dp)
  {
    this.paddingLeft = dp2px(dp);
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
  {
    super.getItemOffsets(outRect, view, parent, state);
    int  pos     = parent.getChildAdapterPosition(view);
    long groupId = groupId(provider.get(pos));
    if (groupId < 0) return;
    if (pos == 0 || isFirstInGroup(pos)) {//同组的第一个才添加padding
      outRect.top = headerHeight;
    } else {
      outRect.top = 0;
    }
  }

  @Override
  public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state)
  {
    super.onDrawOver(c, parent, state);
    int itemCount  = state.getItemCount();
    int childCount = parent.getChildCount();
    int left       = parent.getPaddingLeft();
    int right      = parent.getWidth() - parent.getPaddingRight();

    long preGroupId, groupId = -1;
    for (int i = 0; i < childCount; i++) {
      View view     = parent.getChildAt(i);
      int  position = parent.getChildAdapterPosition(view);

      // can't understand! sometimes position is -1
      if (position < 0) continue;

      preGroupId = groupId;
      String group = provider.get(position);
      groupId = groupId(group);
      if (groupId < 0 || groupId == preGroupId) continue;

      if (TextUtils.isEmpty(group)) continue;
      String textLine = group.toUpperCase();

      int   viewBottom = view.getBottom();
      float textY      = Math.max(headerHeight, view.getTop());
      if (position + 1 < itemCount) {
        //下一个和当前不一样移动当前
        long nextGroupId = groupId(provider.get(position + 1));
        if (nextGroupId != groupId && viewBottom < textY) {
          //组内最后一个view进入了header
          textY = viewBottom;
        }
      }
      c.drawRect(left, textY - headerHeight, right, textY, paint);

      int baseline = (int) ((headerHeight + (fontMetrics.ascent + fontMetrics.descent)) / 2);
      c.drawText(textLine, left + paddingLeft, textY - baseline, textPaint);
    }
  }

  private boolean isFirstInGroup(int pos)
  {
    if (pos == 0) {
      return true;
    } else {
      long prevGroupId = groupId(provider.get(pos - 1));
      long groupId     = groupId(provider.get(pos));
      return prevGroupId != groupId;
    }
  }

  private long groupId(String group)
  {
    return group != null && group.length() > 0 ? Character.toUpperCase(group.charAt(0)) : -1;
  }


  private static int dp2px(float dp)
  {
    DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
  }
}
