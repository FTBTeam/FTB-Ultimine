package dev.ftb.mods.ftbultimine.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ChooseConfigScreen extends BaseScreen {
    private static final int BTN_HEIGHT = 22;

    @Override
    public void addWidgets() {
        add(new TextField(this).setText(Component.translatable("key.ftbultimine")).addFlags(Theme.CENTERED));

        add(new TextButton(this, Component.translatable("ftbultimine.client_settings"), Icons.PLAYER,
                (widget, button) -> FTBUltimineClient.editClientConfig()));
        add(new TextButton(this, Component.translatable("ftbultimine.server_settings"), Icons.GLOBE,
                (widget, button) -> FTBUltimineClient.editServerConfig()));
        add(new TextButton(this, CommonComponents.GUI_BACK, Icons.BACK,
                (widget, button) -> closeGui()));
    }

    @Override
    public void alignWidgets() {
        int ySize = BTN_HEIGHT * 3 + 10;
        int xSize = 50 + widgets.stream().map(w -> getTheme().getStringWidth(w.getTitle())).max(Integer::compare).orElse(100);
        int yPos = (getScreen().getGuiScaledHeight() - ySize) / 2;
        for (Widget w : widgets) {
            w.setPosAndSize((getScreen().getGuiScaledWidth() - xSize) / 2, yPos, xSize, BTN_HEIGHT);
            yPos += BTN_HEIGHT + 5;
        }
    }

    @Override
    public boolean onInit() {
        return setFullscreen();
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
    }

    private static class TextButton extends SimpleTextButton {
        public interface Callback {
            void onClicked(TextButton widget, MouseButton button);
        }

        private final Callback callback;

        public TextButton(Panel panel, Component txt, Icon icon, Callback callback) {
            super(panel, txt, icon);
            this.callback = callback;
        }

        @Override
        public void onClicked(MouseButton button) {
            callback.onClicked(this, button);
        }

        @Override
        public boolean renderTitleInCenter() {
            return true;
        }
    }
}
