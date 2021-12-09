package com.etheller.warsmash.parsers.fdf.frames;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.etheller.warsmash.parsers.fdf.GameUI;
import com.etheller.warsmash.parsers.fdf.datamodel.FramePoint;
import com.etheller.warsmash.parsers.fdf.datamodel.TextJustify;

public class ListBoxFrame extends ControlFrame implements ScrollBarFrame.ScrollBarChangeListener {
    // TODO where are these colors in the UI definition files?
    private static final Color SELECT_COLOR = Color.BLUE;
    private static final Color MOUSE_OVER_HIGHLIGHT_COLOR = new Color(0.3f, 0.3f, 1.0f, 0.25f);

    private final List<String> listItems = new ArrayList<>();
    private final List<SingleStringFrame> stringFrames = new ArrayList<>();
    private BitmapFont frameFont;
    private float listBoxBorder;
    private int selectedIndex = -1;
    private int mouseOverIndex = -1;

    private final TextureFrame selectionFrame;
    private final TextureFrame mouseHighlightFrame;
    private GameUI gameUI;
    private Viewport viewport;
    private Runnable onSelect;
    private ScrollBarFrame scrollBarFrame;

    public ListBoxFrame(final String name, final UIFrame parent, final Viewport viewport) {
        super(name, parent);
        this.listBoxBorder = GameUI.convertX(viewport, 0.01f);
        this.selectionFrame = new TextureFrame(null, this, false, null);
        this.mouseHighlightFrame = new TextureFrame(null, this, false, null);
        final Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
        pixmap.setColor(SELECT_COLOR);
        pixmap.fill();
        this.selectionFrame.setTexture(new Texture(pixmap));
        final Pixmap mousePixmap = new Pixmap(1, 1, Format.RGBA8888);
        mousePixmap.setColor(MOUSE_OVER_HIGHLIGHT_COLOR);
        mousePixmap.fill();
        this.mouseHighlightFrame.setTexture(new Texture(mousePixmap));
    }

    public void setScrollBarFrame(ScrollBarFrame scrollBarFrame) {
        this.scrollBarFrame = scrollBarFrame;
        // TODO might be a better place to add these set points, but we definitely need them
        scrollBarFrame.addSetPoint(new SetPoint(FramePoint.TOPRIGHT, this, FramePoint.TOPRIGHT, -listBoxBorder, -listBoxBorder));
        scrollBarFrame.addSetPoint(new SetPoint(FramePoint.BOTTOMRIGHT, this, FramePoint.BOTTOMRIGHT, -listBoxBorder, listBoxBorder));
        scrollBarFrame.setChangeListener(this);
    }

    public ScrollBarFrame getScrollBarFrame() {
        return scrollBarFrame;
    }

    public void setListBoxBorder(final float listBoxBorder) {
        this.listBoxBorder = listBoxBorder;
    }

    public float getListBoxBorder() {
        return this.listBoxBorder;
    }

    public void setFrameFont(final BitmapFont frameFont) {
        this.frameFont = frameFont;
    }

    @Override
    protected void innerPositionBounds(final GameUI gameUI, final Viewport viewport) {
        this.gameUI = gameUI;
        this.viewport = viewport;
        super.innerPositionBounds(gameUI, viewport);
        updateUI(gameUI, viewport);
    }

    private void positionChildren(final GameUI gameUI, final Viewport viewport) {
        for (final SingleStringFrame frame : this.stringFrames) {
            frame.positionBounds(gameUI, viewport);
        }
        this.selectionFrame.positionBounds(gameUI, viewport);
        this.mouseHighlightFrame.positionBounds(gameUI, viewport);
        if (scrollBarFrame != null) {
            this.scrollBarFrame.positionBounds(gameUI, viewport);
        }
    }

    @Override
    protected void internalRender(final SpriteBatch batch, final BitmapFont baseFont, final GlyphLayout glyphLayout) {
        super.internalRender(batch, baseFont, glyphLayout);
        this.selectionFrame.render(batch, baseFont, glyphLayout);
        this.mouseHighlightFrame.render(batch, baseFont, glyphLayout);
        for (final SingleStringFrame frame : this.stringFrames) {
            frame.render(batch, baseFont, glyphLayout);
        }
        if (scrollBarFrame != null) {
            scrollBarFrame.render(batch, baseFont, glyphLayout);
        }
    }

    public void addItem(final String item, final GameUI gameUI, final Viewport viewport) {
        this.listItems.add(item);
        updateUI(gameUI, viewport);
    }

    public void setItems(final List<String> items, final GameUI gameUI, final Viewport viewport) {
        this.listItems.clear();
        this.listItems.addAll(items);
        updateUI(gameUI, viewport);
    }

    public void removeItem(final String item, final GameUI gameUI, final Viewport viewport) {
        this.listItems.remove(item);
        updateUI(gameUI, viewport);
    }

    public void removeItem(final int index, final GameUI gameUI, final Viewport viewport) {
        this.listItems.remove(index);
        updateUI(gameUI, viewport);
    }

    public void setSelectedIndex(final int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    public String getSelectedItem() {
        if(selectedIndex < 0 || selectedIndex >= listItems.size()) {
            return null;
        }
        return listItems.get(selectedIndex);
    }

    private void updateUI(final GameUI gameUI, final Viewport viewport) {
        SingleStringFrame prev = null;
        boolean foundSelected = false;
        boolean foundMouseOver = false;
        int numStringFrames = (int)(Math.floor( (renderBounds.height - listBoxBorder*2) / (frameFont.getLineHeight()) ));
        int scrollOffset = computeScrollOffset(numStringFrames);
        if(numStringFrames != stringFrames.size()) {
            this.stringFrames.clear();
            for(int stringFrameIndex = 0; stringFrameIndex < numStringFrames; stringFrameIndex++) {
                final int index = stringFrameIndex + scrollOffset;
                final boolean selected = (index == this.selectedIndex);
                final boolean mousedOver = (index == this.mouseOverIndex);
                final SingleStringFrame stringFrame = new SingleStringFrame("LISTY" + index, this, Color.WHITE,
                        TextJustify.LEFT, TextJustify.MIDDLE, this.frameFont);
                if(index < listItems.size()) {
                    stringFrame.setText(listItems.get(index));
                }
                stringFrame.setWidth(this.renderBounds.width - (this.listBoxBorder * 2));
                stringFrame.setHeight(this.frameFont.getLineHeight());
                if (prev != null) {
                    stringFrame.addSetPoint(new SetPoint(FramePoint.TOPLEFT, prev, FramePoint.BOTTOMLEFT, 0, 0));
                } else {
                    stringFrame.addSetPoint(new SetPoint(FramePoint.TOPLEFT, this, FramePoint.TOPLEFT, this.listBoxBorder,
                            -this.listBoxBorder));
                }
                this.stringFrames.add(stringFrame);
                prev = stringFrame;
                if (selected) {
                    this.selectionFrame
                            .addSetPoint(new SetPoint(FramePoint.TOPLEFT, stringFrame, FramePoint.TOPLEFT, 0, 0));
                    this.selectionFrame
                            .addSetPoint(new SetPoint(FramePoint.BOTTOMRIGHT, stringFrame, FramePoint.BOTTOMRIGHT, 0, 0));
                    foundSelected = true;
                } else if (mousedOver) {
                    this.mouseHighlightFrame
                            .addSetPoint(new SetPoint(FramePoint.TOPLEFT, stringFrame, FramePoint.TOPLEFT, 0, 0));
                    this.mouseHighlightFrame
                            .addSetPoint(new SetPoint(FramePoint.BOTTOMRIGHT, stringFrame, FramePoint.BOTTOMRIGHT, 0, 0));
                    foundMouseOver = true;
                }
            }
        } else {
            for(int stringFrameIndex = 0; stringFrameIndex < numStringFrames; stringFrameIndex++) {
                final int index = stringFrameIndex + scrollOffset;
                final boolean selected = (index == this.selectedIndex);
                final boolean mousedOver = (index == this.mouseOverIndex);
                SingleStringFrame stringFrame = stringFrames.get(stringFrameIndex);
                if(index < listItems.size()) {
                    stringFrame.setText(listItems.get(index));
                }
                if (selected) {
                    this.selectionFrame
                            .addSetPoint(new SetPoint(FramePoint.TOPLEFT, stringFrame, FramePoint.TOPLEFT, 0, 0));
                    this.selectionFrame
                            .addSetPoint(new SetPoint(FramePoint.BOTTOMRIGHT, stringFrame, FramePoint.BOTTOMRIGHT, 0, 0));
                    foundSelected = true;
                } else if (mousedOver) {
                    this.mouseHighlightFrame
                            .addSetPoint(new SetPoint(FramePoint.TOPLEFT, stringFrame, FramePoint.TOPLEFT, 0, 0));
                    this.mouseHighlightFrame
                            .addSetPoint(new SetPoint(FramePoint.BOTTOMRIGHT, stringFrame, FramePoint.BOTTOMRIGHT, 0, 0));
                    foundMouseOver = true;
                }
            }
        }
        this.selectionFrame.setVisible(foundSelected);
        this.mouseHighlightFrame.setVisible(foundMouseOver);
        positionChildren(gameUI, viewport);
    }

    private int computeScrollOffset(int numStringFrames) {
        int scrollOffset;
        if(scrollBarFrame != null && listItems.size() > numStringFrames){
            scrollOffset = (int)Math.ceil(((100 - scrollBarFrame.getValue()) / 100f) * (listItems.size() - numStringFrames));
        } else {
            scrollOffset = 0;
        }
        return scrollOffset;
    }

    @Override
    public UIFrame touchDown(final float screenX, final float screenY, final int button) {
        if (isVisible() && this.renderBounds.contains(screenX, screenY)) {
            if(scrollBarFrame!=null) {
                UIFrame sliderFrameChildUnderMouse = scrollBarFrame.touchDown(screenX, screenY, button);
                if (sliderFrameChildUnderMouse != null) {
                    return sliderFrameChildUnderMouse;
                }
            }
            int index = 0;
            for (final SingleStringFrame stringFrame : this.stringFrames) {
                if (stringFrame.getRenderBounds().contains(screenX, screenY)) {
                    this.selectedIndex = index + computeScrollOffset(stringFrames.size());
                }
                index++;
            }
            updateUI(this.gameUI, this.viewport);
            if (this.onSelect != null) {
                this.onSelect.run();
            }
            return this;
        }
        return super.touchDown(screenX, screenY, button);
    }

    @Override
    public UIFrame touchUp(float screenX, float screenY, int button) {
        if (isVisible() && this.renderBounds.contains(screenX, screenY)) {
            if (scrollBarFrame != null) {
                UIFrame sliderFrameChildUnderMouse = scrollBarFrame.touchDown(screenX, screenY, button);
                if (sliderFrameChildUnderMouse != null) {
                    return sliderFrameChildUnderMouse;
                }
            }
        }
        return super.touchUp(screenX, screenY, button);
    }

    @Override
    public UIFrame getFrameChildUnderMouse(final float screenX, final float screenY) {
        if (isVisible() && this.renderBounds.contains(screenX, screenY)) {
            if(scrollBarFrame!=null) {
                UIFrame sliderFrameChildUnderMouse = scrollBarFrame.getFrameChildUnderMouse(screenX, screenY);
                if (sliderFrameChildUnderMouse != null) {
                    return sliderFrameChildUnderMouse;
                }
            }
            int index = 0;
            int mouseOverIndex = -1;
            for (final SingleStringFrame stringFrame : this.stringFrames) {
                if (stringFrame.getRenderBounds().contains(screenX, screenY)) {
                    mouseOverIndex = index;
                }
                index++;
            }
            if (this.mouseOverIndex != mouseOverIndex) {
                this.mouseOverIndex = mouseOverIndex + computeScrollOffset(stringFrames.size());
                updateUI(this.gameUI, this.viewport);
            }
        }
        return super.getFrameChildUnderMouse(screenX, screenY);
    }

    public void setOnSelect(final Runnable onSelect) {
        this.onSelect = onSelect;
    }

    @Override
    public void onChange(GameUI gameUI, Viewport uiViewport, int newValue) {
        updateUI(gameUI, uiViewport);
    }
}
