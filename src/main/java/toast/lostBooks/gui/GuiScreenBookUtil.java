package toast.lostBooks.gui;

import com.google.common.collect.Lists;
import com.google.gson.JsonParseException;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import toast.lostBooks.LostBooks;
import toast.lostBooks.MessageCurrPage;
import toast.lostBooks.helper.BookHelper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiScreenBookUtil extends GuiScreen {
	private static final Logger logger = LogManager.getLogger();
	private static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
	public final ItemStack book;
	private final boolean pauseGame;
	/**
	 * The player editing the book
	 */
	private final EntityPlayer editingPlayer;
	/**
	 * Whether the book is signed or can still be edited
	 */
	private final boolean bookIsUnsigned;
	private final int bookImageWidth = 192;
	private boolean bookIsModified;
	private int cachedPage = -1;
	/**
	 * Update ticks since the gui was opened
	 */
	private int updateCount;
	public final int bookImageHeight = 192;
	private int bookTotalPages = 1;
	private int currPage, lastCurrPage;
	private NBTTagList bookPages;
	private String bookTitle = "";
	private NextPageButton buttonNextPage;
	private NextPageButton buttonPreviousPage;
	private GuiButton buttonDone;
	/**
	 * Determines if the signing screen is open
	 */
	private boolean bookGettingSigned;
	private GuiButton buttonSign;
	private GuiButton buttonFinalize;
	public GuiButton buttonCancel;
	private List<ITextComponent> cachedComponents;

	public GuiScreenBookUtil(GuiScreenBook parentScreen, boolean bookmark, boolean pauseGame) {
		this.editingPlayer = ObfuscationReflectionHelper.getPrivateValue(GuiScreenBook.class, parentScreen, "editingPlayer");
		this.book = ObfuscationReflectionHelper.getPrivateValue(GuiScreenBook.class, parentScreen, "book");
		this.bookIsUnsigned = ((Boolean) ObfuscationReflectionHelper.getPrivateValue(GuiScreenBook.class, parentScreen, "bookIsUnsigned")).booleanValue();
		this.bookGettingSigned = ((Boolean) ObfuscationReflectionHelper.getPrivateValue(GuiScreenBook.class, parentScreen, "bookGettingSigned")).booleanValue();
		this.pauseGame = pauseGame;

		if (this.book.hasTagCompound()) {
			NBTTagCompound nbttagcompound = this.book.getTagCompound();
			this.bookPages = nbttagcompound.getTagList("pages", 8);

			if (this.bookPages != null) {
				this.bookPages = this.bookPages.copy();
				this.bookTotalPages = this.bookPages.tagCount();

				if (this.bookTotalPages < 1) {
					this.bookTotalPages = 1;
				}
			}
			if (bookmark) {
				this.currPage = BookHelper.getCurrentPage(this.book);
				if (this.currPage < 0 || this.currPage >= this.bookTotalPages) {
					this.currPage = 0;
				}
				this.lastCurrPage = this.currPage;
			}
		}

		if (this.bookPages == null && this.bookIsUnsigned) {
			this.bookPages = new NBTTagList();
			this.bookPages.appendTag(new NBTTagString(""));
			this.bookTotalPages = 1;
		}
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	@Override
	public void updateScreen() {
		super.updateScreen();
		this.updateCount++;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui() {
		this.buttonList.clear();
		Keyboard.enableRepeatEvents(true);

		if (this.bookIsUnsigned) {
			this.buttonSign = this.addButton(new GuiButton(3, this.width / 2 - 100, 196, 98, 20, I18n.format("book.signButton")));
			this.buttonDone = this.addButton(new GuiButton(0, this.width / 2 + 2, 196, 98, 20, I18n.format("gui.done")));
			this.buttonFinalize = this.addButton(new GuiButton(5, this.width / 2 - 100, 196, 98, 20, I18n.format("book.finalizeButton")));
			this.buttonCancel = this.addButton(new GuiButton(4, this.width / 2 + 2, 196, 98, 20, I18n.format("gui.cancel")));
		} else {
			this.buttonDone = this.addButton(new GuiButton(0, this.width / 2 - 100, 196, 200, 20, I18n.format("gui.done")));
		}

		int i = (this.width - this.bookImageWidth) / 2;
		byte b0 = 2;
		this.buttonList.add(this.buttonNextPage = new NextPageButton(1, i + 120, b0 + 154, true));
		this.buttonList.add(this.buttonPreviousPage = new NextPageButton(2, i + 38, b0 + 154, false));
		this.updateButtons();
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	private void updateButtons() {
		this.buttonNextPage.visible = !this.bookGettingSigned && (this.currPage < this.bookTotalPages - 1 || this.bookIsUnsigned);
		this.buttonPreviousPage.visible = !this.bookGettingSigned && this.currPage > 0;
		this.buttonDone.visible = !this.bookIsUnsigned || !this.bookGettingSigned;

		if (this.bookIsUnsigned) {
			this.buttonSign.visible = !this.bookGettingSigned;
			this.buttonCancel.visible = this.bookGettingSigned;
			this.buttonFinalize.visible = this.bookGettingSigned;
			this.buttonFinalize.enabled = !this.bookTitle.trim().isEmpty();
		}
	}

	private void sendBookToServer(boolean publish) {
		if (this.bookIsUnsigned && this.bookIsModified) {
			if (this.bookPages != null) {
				while (this.bookPages.tagCount() > 1) {
					String s = this.bookPages.getStringTagAt(this.bookPages.tagCount() - 1);

					if (!s.isEmpty()) {
						break;
					}

					this.bookPages.removeTag(this.bookPages.tagCount() - 1);
				}

				if (this.book.hasTagCompound()) {
					NBTTagCompound nbttagcompound = this.book.getTagCompound();
					nbttagcompound.setTag("pages", this.bookPages);
				} else {
					this.book.setTagInfo("pages", this.bookPages);
				}

				String s1 = "MC|BEdit";

				if (publish) {
					s1 = "MC|BSign";
					this.book.setTagInfo("author", new NBTTagString(this.editingPlayer.getName()));
					this.book.setTagInfo("title", new NBTTagString(this.bookTitle.trim()));
				}

				PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
				packetbuffer.writeItemStack(this.book);
				this.mc.getConnection().sendPacket(new CPacketCustomPayload(s1, packetbuffer));
			}
		}
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled) {
			if (button.id == 0) {
				this.mc.displayGuiScreen(null);
				this.sendBookToServer(false);
			} else if (button.id == 3 && this.bookIsUnsigned) {
				this.bookGettingSigned = true;
			} else if (button.id == 1) {
				if (this.currPage < this.bookTotalPages - 1) {
					++this.currPage;
				} else if (this.bookIsUnsigned) {
					this.addNewPage();

					if (this.currPage < this.bookTotalPages - 1) {
						++this.currPage;
					}
				}

				this.updateAndSendCurrPage();
			} else if (button.id == 2) {
				if (this.currPage > 0) {
					--this.currPage;
					this.updateAndSendCurrPage();
				}
			} else if (button.id == 5 && this.bookGettingSigned) {
				this.sendBookToServer(true);
				this.mc.displayGuiScreen(null);
			} else if (button.id == 4 && this.bookGettingSigned) {
				this.bookGettingSigned = false;
			}

			this.updateButtons();
		}
	}

	// Updates the book's current page tag and sends it to the server.
	private void updateAndSendCurrPage() {
		if (this.lastCurrPage != this.currPage) {
			this.lastCurrPage = this.currPage;
			BookHelper.setCurrentPage(this.book, this.currPage);
			LostBooks.CHANNEL.sendToServer(new MessageCurrPage(this.currPage));
		}
	}

	private void addNewPage() {
		if (this.bookPages != null && this.bookPages.tagCount() < 50) {
			this.bookPages.appendTag(new NBTTagString(""));
			++this.bookTotalPages;
			this.bookIsModified = true;
		}
	}

	/**
	 * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);

		if (this.bookIsUnsigned) {
			if (this.bookGettingSigned) {
				this.keyTypedInTitle(typedChar, keyCode);
			} else {
				this.keyTypedInBook(typedChar, keyCode);
			}
		}
	}

	/**
	 * Processes keystrokes when editing the text of a book
	 */
	private void keyTypedInBook(char typedChar, int keyCode) {
		if (GuiScreen.isKeyComboCtrlV(keyCode)) {
			this.pageInsertIntoCurrent(GuiScreen.getClipboardString());
		} else {
			switch (keyCode) {
				case 14:
					String s = this.pageGetCurrent();

					if (!s.isEmpty()) {
						this.pageSetCurrent(s.substring(0, s.length() - 1));
					}

					return;
				case 28:
				case 156:
					this.pageInsertIntoCurrent("\n");
					return;
				default:

					if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
						this.pageInsertIntoCurrent(Character.toString(typedChar));
					}
			}
		}
	}

	/**
	 * Processes keystrokes when editing the title of a book
	 */
	private void keyTypedInTitle(char typedChar, int keyCode) throws IOException {
		switch (keyCode) {
			case 14:

				if (!this.bookTitle.isEmpty()) {
					this.bookTitle = this.bookTitle.substring(0, this.bookTitle.length() - 1);
					this.updateButtons();
				}

				return;
			case 28:
			case 156:

				if (!this.bookTitle.isEmpty()) {
					this.sendBookToServer(true);
					this.mc.displayGuiScreen(null);
				}

				return;
			default:

				if (this.bookTitle.length() < 16 && ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
					this.bookTitle = this.bookTitle + typedChar;
					this.updateButtons();
					this.bookIsModified = true;
				}
		}
	}

	private String pageGetCurrent() {
		return this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount() ? this.bookPages.getStringTagAt(this.currPage) : "";
	}

	private void pageSetCurrent(String nbt) {
		if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount()) {
			this.bookPages.set(this.currPage, new NBTTagString(nbt));
			this.bookIsModified = true;
		}
	}

	private void pageInsertIntoCurrent(String text) {
		String s1 = this.pageGetCurrent();
		String s2 = s1 + text;
		int i = this.fontRenderer.getWordWrappedHeight(s2 + "" + TextFormatting.BLACK + "_", 118);

		if (i <= 118 && s2.length() < 256) {
			this.pageSetCurrent(s2);
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(GuiScreenBookUtil.bookGuiTextures);
		int i = (this.width - 192) / 2;
		int j = 2;
		this.drawTexturedModalRect(i, 2, 0, 0, 192, 192);

		if (this.bookGettingSigned) {
			String s = this.bookTitle;

			if (this.bookIsUnsigned) {
				if (this.updateCount / 6 % 2 == 0) {
					s = s + "" + TextFormatting.BLACK + "_";
				} else {
					s = s + "" + TextFormatting.GRAY + "_";
				}
			}

			String s1 = I18n.format("book.editTitle");
			int k = this.fontRenderer.getStringWidth(s1);
			this.fontRenderer.drawString(s1, i + 36 + (116 - k) / 2, 34, 0);
			int l = this.fontRenderer.getStringWidth(s);
			this.fontRenderer.drawString(s, i + 36 + (116 - l) / 2, 50, 0);
			String s2 = I18n.format("book.byAuthor", this.editingPlayer.getName());
			int i1 = this.fontRenderer.getStringWidth(s2);
			this.fontRenderer.drawString(TextFormatting.DARK_GRAY + s2, i + 36 + (116 - i1) / 2, 60, 0);
			String s3 = I18n.format("book.finalizeWarning");
			this.fontRenderer.drawSplitString(s3, i + 36, 82, 116, 0);
		} else {
			String s4 = I18n.format("book.pageIndicator", this.currPage + 1, this.bookTotalPages);
			String s5 = "";

			if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount()) {
				s5 = this.bookPages.getStringTagAt(this.currPage);
			}

			if (this.bookIsUnsigned) {
				if (this.fontRenderer.getBidiFlag()) {
					s5 = s5 + "_";
				} else if (this.updateCount / 6 % 2 == 0) {
					s5 = s5 + "" + TextFormatting.BLACK + "_";
				} else {
					s5 = s5 + "" + TextFormatting.GRAY + "_";
				}
			} else if (this.cachedPage != this.currPage) {
				if (ItemWrittenBook.validBookTagContents(this.book.getTagCompound())) {
					try {
						ITextComponent itextcomponent = ITextComponent.Serializer.jsonToComponent(s5);
						this.cachedComponents = itextcomponent != null ? GuiUtilRenderComponents.splitText(itextcomponent, 116, this.fontRenderer, true, true) : null;
					}
					catch (JsonParseException var13) {
						this.cachedComponents = null;
					}
				} else {
					TextComponentString textcomponentstring = new TextComponentString(TextFormatting.DARK_RED + "* Invalid book tag *");
					this.cachedComponents = Lists.newArrayList(textcomponentstring);
				}

				this.cachedPage = this.currPage;
			}

			int j1 = this.fontRenderer.getStringWidth(s4);
			this.fontRenderer.drawString(s4, i - j1 + 192 - 44, 18, 0);

			if (this.cachedComponents == null) {
				this.fontRenderer.drawSplitString(s5, i + 36, 34, 116, 0);
			} else {
				int k1 = Math.min(128 / this.fontRenderer.FONT_HEIGHT, this.cachedComponents.size());

				for (int l1 = 0; l1 < k1; ++l1) {
					ITextComponent itextcomponent2 = this.cachedComponents.get(l1);
					this.fontRenderer.drawString(itextcomponent2.getUnformattedText(), i + 36, 34 + l1 * this.fontRenderer.FONT_HEIGHT, 0);
				}

				ITextComponent itextcomponent1 = this.getClickedComponentAt(mouseX, mouseY);

				if (itextcomponent1 != null) {
					this.handleComponentHover(itextcomponent1, mouseX, mouseY);
				}
			}
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Nullable
	public ITextComponent getClickedComponentAt(int p_175385_1_, int p_175385_2_) {
		if (this.cachedComponents == null) {
			return null;
		} else {
			int i = p_175385_1_ - (this.width - 192) / 2 - 36;
			int j = p_175385_2_ - 2 - 16 - 16;

			if (i >= 0 && j >= 0) {
				int k = Math.min(128 / this.fontRenderer.FONT_HEIGHT, this.cachedComponents.size());

				if (i <= 116 && j < this.mc.fontRenderer.FONT_HEIGHT * k + k) {
					int l = j / this.mc.fontRenderer.FONT_HEIGHT;

					if (l >= 0 && l < this.cachedComponents.size()) {
						ITextComponent itextcomponent = this.cachedComponents.get(l);
						int i1 = 0;

						for (ITextComponent itextcomponent1 : itextcomponent) {
							if (itextcomponent1 instanceof TextComponentString) {
								i1 += this.mc.fontRenderer.getStringWidth(((TextComponentString) itextcomponent1).getText());

								if (i1 > i) {
									return itextcomponent1;
								}
							}
						}
					}

					return null;
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
	}

	@SideOnly(Side.CLIENT)
	static class NextPageButton extends GuiButton {
		private final boolean isForward;

		public NextPageButton(int buttonId, int x, int y, boolean isForwardIn) {
			super(buttonId, x, y, 23, 13, "");
			this.isForward = isForwardIn;
		}

		/**
		 * Draws this button to the screen.
		 */
		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
			if (this.visible) {
				boolean flag = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				mc.getTextureManager().bindTexture(GuiScreenBookUtil.bookGuiTextures);
				int k = 0;
				int l = 192;

				if (flag) {
					k += 23;
				}

				if (!this.isForward) {
					l += 13;
				}

				this.drawTexturedModalRect(this.x, this.y, k, l, 23, 13);
			}
		}
	}

	/**
	 * Returns true if this GUI should pause the game when it is displayed in single-player
	 */
	@Override
	public boolean doesGuiPauseGame() {
		return this.pauseGame;
	}
}
