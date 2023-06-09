package illyena.gilding.config.gui;

import com.google.common.collect.ImmutableList;
import illyena.gilding.config.network.ConfigNetworking;
import illyena.gilding.config.option.ConfigOption;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.OrderableTooltip;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

import static illyena.gilding.GildingInit.*;

@Environment(EnvType.CLIENT)
public abstract class ConfigScreen extends Screen {
    protected final boolean isMinecraft;
    protected Screen parent;
    protected String modId;

    Map< Element, ConfigOption<?>> map = new HashMap<>();
    Text WORLD_GEN_INFO = translationKeyOf("menu", "world_gen_config.info");

    protected ConfigScreen(String modId, Screen parent) {
        super(new TranslatableText("menu." + modId + ".title"));
        this.isMinecraft = (double)(new Random()).nextFloat() < 1.0E-4;
        this.parent = parent;
        this.modId = modId;
    }

    protected List<ConfigOption<?>> getConfigs(String modId) {
        List<ConfigOption<?>> list = new ArrayList<>();
        List<Identifier> ids = new ArrayList<>(ConfigOption.CONFIG.getIds().stream().filter(id -> id.getNamespace().equals(modId)).toList());
        Collections.sort(ids);
        for (Identifier id : ids) {
            list.add(ConfigOption.getConfig(id));
        }
        return list;
    }

    protected void init() {
        this.initSync();

        int l = this.height / 4 + 48;
        this.initMultiWidgets(this.modId);
        this.initBackWidget(l);
        this.initReturnWidget(l);
    }

    protected void initBackWidget(int l) {
        assert this.client != null;
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, l + 72 + 12, 98, 20,
                ScreenTexts.BACK, (button) -> this.client.setScreen(this.parent)));
    }

    protected void initReturnWidget(int l) {
        assert this.client != null;
        if (this.client.world != null) {
            this.addDrawableChild(new ButtonWidget(this.width / 2 + 2, l + 72 +12, 98, 20,
                    new TranslatableText("menu.returnToGame"), button -> this.close()));
        } else {
            this.addDrawableChild(new ButtonWidget(this.width / 2 + 2, l + 72 + 12, 98, 20,
                    new TranslatableText("gui.toTitle"), (button) -> this.client.setScreen(new TitleScreen())));
        }
    }

    protected void initMultiWidgets(String modId) {
        assert this.client != null;
        int i = 0;
        for (ConfigOption<?> config : getConfigs(modId)) {
            int j = this.width / 2 - 155 + i % 2 * 160;
            int k = this.height / 6 - 12 + 24 * (i >> 1) + 48;
            ClickableWidget drawable;
            if (!this.inactivateButton(config)) {
                drawable = this.addDrawableChild(config.createButton(j, k, 150));
            } else {
                drawable = this.addDrawableChild(this.createDeadButton(config, j, k, 150));
            }
            this.map.put(drawable, config);
            ++i;
        }
    }

    protected void initSync() {
        assert this.client != null;
        if (this.client.world != null) {
            ClientPlayNetworking.send(ConfigNetworking.CONFIG_RETRIEVE_C2S, PacketByteBufs.create());
        }
    }

    protected boolean inactivateButton(ConfigOption<?> config) {
        return switch (config.getAccessType()) {
            case BOTH -> false;
            case CLIENT -> this.client == null || this.client.world == null;
            case SERVER, WORLD_GEN -> this.client != null && this.client.world != null;
        };
    }

    protected ClickableWidget createDeadButton(ConfigOption<?> config, int x, int y, int width) {
        ButtonWidget.TooltipSupplier tooltips = new ButtonWidget.TooltipSupplier() {
            private static final Text NO_SERVER_TEXT = new TranslatableText("menu." + SUPER_MOD_ID + ".no_server.tooltip");
            @Override
            public void onTooltip(ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY) {
                if (button.active) {
                    ConfigScreen.this.renderTooltip(matrices, NO_SERVER_TEXT, mouseX, mouseY);
                }
            }
            public void supply(Consumer<Text> consumer) { consumer.accept(this.NO_SERVER_TEXT); }
        };
        return new ButtonWidget( x, y, width, 20, config.getButtonText(), button -> {}, tooltips) {
            @Override
            protected int getYImage(boolean hovered) { return 0; }

        };

    }

    public void close() {
        ConfigOption.getConfigs(this.modId).forEach(ConfigOption::sync);
        super.close();
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        float g = 1.0f;
        int l = MathHelper.ceil(g * 255.0F) << 24;
        if ((l & -67108864) != 0) {
            for (Element element : this.children()) {
                if (element instanceof ClickableWidget clickable) {
                    clickable.setAlpha(g);
                    drawInfo(clickable, matrices);
                }
            }
            super.render(matrices, mouseX, mouseY, delta);
        }
        List<OrderedText> list = getHoveredButtonTooltip(mouseX, mouseY);
        if (list != null) {
            this.renderOrderedTooltip(matrices, list, mouseX, mouseY);
        }
    }

    private void drawInfo(ClickableWidget widget, MatrixStack matrices) {
        if (this.map.get(widget) != null) {
            switch (this.map.get(widget).getAccessType()) {
                case WORLD_GEN -> drawTextWithShadow(matrices, this.textRenderer, WORLD_GEN_INFO, widget.x + 4, widget.y + 22, Color.GRAY.getRGB());
                default -> { }
            }
        }
    }
    public List<OrderedText> getHoveredButtonTooltip(int mouseX, int mouseY) {
        Optional<ClickableWidget> optional = this.getHoveredButton(mouseX, mouseY);
        return optional.isPresent() && optional.get() instanceof OrderableTooltip ? ((OrderableTooltip)optional.get()).getOrderedTooltip() : ImmutableList.of();
    }

    public Optional<ClickableWidget> getHoveredButton(double mouseX, double mouseY) {
        for (Element element : this.children()) {
            if (element instanceof ClickableWidget widget) {
                if (widget.isMouseOver(mouseX, mouseY));
                return Optional.of(widget);
            }
        }
        return Optional.empty();
    }
}

