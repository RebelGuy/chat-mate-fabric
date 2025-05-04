package dev.rebel.chatmate;

import dev.rebel.chatmate.api.models.account.LoginRequest;
import dev.rebel.chatmate.api.proxy.AccountEndpointProxy;
import dev.rebel.chatmate.api.proxy.EndpointProxy;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.mixin.TextFieldWidgetAccessor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class ChatMateSettingsScreen extends Screen {
  private static int paddingTop = 20;
  private static int paddingSide = 20;
  private static int paddingBetweenX = 20;
  private static int paddingBetweenY = 5;
  private static int buttonWidth = 60;
  private static int inputHeight = 20;
  private static int buttonHeight = 10;

  private final Screen parent;
  private final Config config;
  private final AccountEndpointProxy accountEndpointProxy;

  private @Nullable String error = null;

  protected ChatMateSettingsScreen(Screen parent) {
    super(Text.literal("ChatMate"));

    this.parent = parent;
    this.config = ChatMate.INSTANCE.config;
    this.accountEndpointProxy = ChatMate.INSTANCE.accountEndpointProxy;
  }

  @Override
  public void close() {
    if (super.client != null) {
      super.client.setScreen(this.parent);
    }
  }

  @Override
  protected void init() {
    int inputWidth = (super.width - paddingSide * 2 - paddingBetweenX) / 2;

    boolean isLoggedIn = this.config.getLoginInfoEmitter().get().loginToken != null;
    TextFieldWidget usernameInput = new TextFieldWidget(super.textRenderer, paddingSide, paddingTop, inputWidth, inputHeight, Text.empty());
    usernameInput.setPlaceholder(Text.literal("Username"));
    usernameInput.setText(this.config.getLoginInfoEmitter().get().username);
    
    PasswordFieldWidget passwordInput = new PasswordFieldWidget(super.textRenderer, paddingSide + inputWidth + paddingBetweenX, paddingTop, inputWidth, inputHeight, Text.empty());
    passwordInput.setPlaceholder(Text.literal("Password"));

    PressableTextWidget loginButton = new PressableTextWidget(
        paddingSide,
        paddingTop + inputHeight + paddingBetweenY,
        buttonWidth,
        buttonHeight,
        Text.literal("Login"),
        button -> this.onLogin(usernameInput.getText(), passwordInput.getText()),
        super.textRenderer
    );
    PressableTextWidget logoutButton = new PressableTextWidget(
        paddingSide,
        paddingTop + inputHeight + paddingBetweenY,
        buttonWidth,
        buttonHeight,
        Text.literal("Logout"),
        button -> this.onLogout(),
        super.textRenderer
    );

    super.addDrawableChild(usernameInput);
    super.addDrawableChild(passwordInput);

    if (!isLoggedIn) {
      super.addDrawableChild(loginButton);
    } else {
      super.addDrawableChild(logoutButton);
    }
  }
  
  private void onLogin(String username, String password) {
    this.accountEndpointProxy.loginAsync(
        new LoginRequest(username, password),
        loginResponseData -> {
          this.config.getLoginInfoEmitter().set(new Config.LoginInfo(username, loginResponseData.displayName, loginResponseData.loginToken));
          this.error = null;
          super.clearAndInit();
        },
        e -> {
          this.error = EndpointProxy.getApiErrorMessage(e);
        }
    );
  }
  
  private void onLogout() {
    this.config.getLoginInfoEmitter().set(new Config.LoginInfo(null, null, null));
    this.error = null;
    super.clearAndInit();
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    super.render(context, mouseX, mouseY, delta);

    boolean isLoggedIn = this.config.getLoginInfoEmitter().get().loginToken != null;
    @Nullable String error = this.error;
    if (error == null && !isLoggedIn) {
      error = "You are not logged in.";
    }
    if (error != null) {
      context.drawWrappedText(super.textRenderer, Text.literal(error), 20, paddingTop + inputHeight + paddingBetweenY + buttonHeight + paddingBetweenY, super.width - 20 - 20, 0xFF0000, false);
    } else if (isLoggedIn) {
      String displayName = this.config.getLoginInfoEmitter().get().getDisplayName();
      context.drawWrappedText(super.textRenderer, Text.literal("Logged in as " + displayName), 20, paddingTop + inputHeight + paddingBetweenY + buttonHeight + paddingBetweenY, super.width - 20 - 20, 0x00FF00, false);
    }
  }

  private static class PasswordFieldWidget extends TextFieldWidget {
    public PasswordFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
      super(textRenderer, x, y, width, height, text);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
      String text = super.getText();
      ((TextFieldWidgetAccessor)this).setTextUnsafe("*".repeat(text.length()));
      super.renderWidget(context, mouseX, mouseY, delta);
      ((TextFieldWidgetAccessor)this).setTextUnsafe(text);
    }
  }
}
