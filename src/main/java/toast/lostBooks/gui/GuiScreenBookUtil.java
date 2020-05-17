package toast.lostBooks.gui;

import com.mojang.realmsclient.gui.ChatFormatting;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
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

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiScreenBookUtil extends GuiScreen {
	private static final Logger logger = LogManager.getLogger();
	public static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
	public final boolean pauseGame;
	/**
	 * The player editing the book
	 */
	public final EntityPlayer editingPlayer;
	public final ItemStack book;
	/**
	 * Whether the book is signed or can still be edited
	 */
	public final boolean bookIsUnsigned;
	public boolean hasUpdated;
	public boolean settingTitle;
	/**
	 * Update ticks since the gui was opened
	 */
	public int updateCount;
	public final int bookImageWidth = 192;
	public final int bookImageHeight = 192;
	public int bookTotalPages = 1;
	public int currPage, lastCurrPage;
	public NBTTagList bookPages;
	public String bookTitle = "";
	public NextPageButton buttonNextPage;
	public NextPageButton buttonPreviousPage;
	public GuiButton buttonDone;
	/**
	 * The GuiButton to sign this book.
	 */
	public GuiButton buttonSign;
	public GuiButton buttonFinalize;
	public GuiButton buttonCancel;

	public GuiScreenBookUtil(GuiScreenBook parentScreen, boolean bookmark, boolean pauseGame) {
		this.editingPlayer = ObfuscationReflectionHelper.getPrivateValue(GuiScreenBook.class, parentScreen, "field_146468_g", "editingPlayer");
		this.book = ObfuscationReflectionHelper.getPrivateValue(GuiScreenBook.class, parentScreen, "field_146474_h", "book");
		this.bookIsUnsigned = ((Boolean) ObfuscationReflectionHelper.getPrivateValue(GuiScreenBook.class, parentScreen, "field_146475_i", "bookIsUnsigned")).booleanValue();
		this.pauseGame = pauseGame;

		if (this.book.hasTagCompound()) {
			NBTTagCompound nbttagcompound = this.book.getTagCompound();
			this.bookPages = nbttagcompound.getTagList("pages", 8);

			if (this.bookPages != null) {
				this.bookPages = (NBTTagList) this.bookPages.copy();
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
			this.buttonList.add(this.buttonSign = new GuiButton(3, this.width / 2 - 100, 4 + this.bookImageHeight, 98, 20, I18n.format("book.signButton", new Object[0])));
			this.buttonList.add(this.buttonDone = new GuiButton(0, this.width / 2 + 2, 4 + this.bookImageHeight, 98, 20, I18n.format("gui.done", new Object[0])));
			this.buttonList.add(this.buttonFinalize = new GuiButton(5, this.width / 2 - 100, 4 + this.bookImageHeight, 98, 20, I18n.format("book.finalizeButton", new Object[0])));
			this.buttonList.add(this.buttonCancel = new GuiButton(4, this.width / 2 + 2, 4 + this.bookImageHeight, 98, 20, I18n.format("gui.cancel", new Object[0])));
		} else {
			this.buttonList.add(this.buttonDone = new GuiButton(0, this.width / 2 - 100, 4 + this.bookImageHeight, 200, 20, I18n.format("gui.done", new Object[0])));
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
		this.buttonNextPage.visible = !this.settingTitle && (this.currPage < this.bookTotalPages - 1 || this.bookIsUnsigned);
		this.buttonPreviousPage.visible = !this.settingTitle && this.currPage > 0;
		this.buttonDone.visible = !this.bookIsUnsigned || !this.settingTitle;

		if (this.bookIsUnsigned) {
			this.buttonSign.visible = !this.settingTitle;
			this.buttonCancel.visible = this.settingTitle;
			this.buttonFinalize.visible = this.settingTitle;
			this.buttonFinalize.enabled = this.bookTitle.trim().length() > 0;
		}
	}

	private void sendBookToServer(boolean publish) {
		if (this.bookIsUnsigned && this.hasUpdated) {
			if (this.bookPages != null) {
				String s;

				while (this.bookPages.tagCount() > 1) {
					s = this.bookPages.getStringTagAt(this.bookPages.tagCount() - 1);

					if (s.length() != 0) {
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

				s = "MC|BEdit";

				if (publish) {
					s = "MC|BSign";
					this.book.setTagInfo("author", new NBTTagString(this.editingPlayer.getName()));
					this.book.setTagInfo("title", new NBTTagString(this.bookTitle.trim()));
					//                    this.book.func_150996_a(Items.WRITTEN_BOOK);
				}

				//                ByteBuf bytebuf = Unpooled.buffer();

				try {
					PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
					packetbuffer.writeItemStack(this.book);
				}
				catch (Exception exception) {
					GuiScreenBookUtil.logger.error("Couldn\'t send book info", exception);
				}
				//                finally {
				//                    bytebuf.release();
				//                }
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id == 0) {
				this.mc.displayGuiScreen((GuiScreen) null);
				this.sendBookToServer(false);
			} else if (button.id == 3 && this.bookIsUnsigned) {
				this.settingTitle = true;
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
			} else if (button.id == 5 && this.settingTitle) {
				this.sendBookToServer(true);
				this.mc.displayGuiScreen((GuiScreen) null);
			} else if (button.id == 4 && this.settingTitle) {
				this.settingTitle = false;
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
			this.hasUpdated = true;
		}
	}

	/**
	 * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);

		if (this.bookIsUnsigned) {
			if (this.settingTitle) {
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
		switch (typedChar) {
			case 22:
				this.pageInsertIntoCurrent(GuiScreen.getClipboardString());
				return;
			default:
				switch (keyCode) {
					case 14:
						String s = this.pageGetCurrent();

						if (s.length() > 0) {
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
	private void keyTypedInTitle(char typedChar, int keyCode) {
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
					this.mc.displayGuiScreen((GuiScreen) null);
				}

				return;
			default:
				if (this.bookTitle.length() < 16 && ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
					this.bookTitle = this.bookTitle + Character.toString(typedChar);
					this.updateButtons();
					this.hasUpdated = true;
				}
		}
	}

	private String pageGetCurrent() {
		return this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount() ? this.bookPages.getStringTagAt(this.currPage) : "";
	}

	private void pageSetCurrent(String nbt) {
		if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount()) {
			this.bookPages.set(this.currPage, new NBTTagString(nbt));
			this.hasUpdated = true;
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
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(GuiScreenBookUtil.bookGuiTextures);
		int k = (this.width - this.bookImageWidth) / 2;
		byte b0 = 2;
		this.drawTexturedModalRect(k, b0, 0, 0, this.bookImageWidth, this.bookImageHeight);
		String s;
		String s1;
		int l;

		if (this.settingTitle) {
			s = this.bookTitle;

			if (this.bookIsUnsigned) {
				if (this.updateCount / 6 % 2 == 0) {
					s = s + "" + ChatFormatting.BLACK + "_";
				} else {
					s = s + "" + ChatFormatting.GRAY + "_";
				}
			}

			s1 = I18n.format("book.editTitle", new Object[0]);
			l = this.fontRenderer.getStringWidth(s1);
			this.fontRenderer.drawString(s1, k + 36 + (116 - l) / 2, b0 + 16 + 16, 0);
			int i1 = this.fontRenderer.getStringWidth(s);
			this.fontRenderer.drawString(s, k + 36 + (116 - i1) / 2, b0 + 48, 0);
			String s2 = I18n.format("book.byAuthor", new Object[] {this.editingPlayer.getCommandSenderEntity().getName()});
			int j1 = this.fontRenderer.getStringWidth(s2);
			this.fontRenderer.drawString(ChatFormatting.DARK_GRAY + s2, k + 36 + (116 - j1) / 2, b0 + 48 + 10, 0);
			String s3 = I18n.format("book.finalizeWarning", new Object[0]);
			this.fontRenderer.drawSplitString(s3, k + 36, b0 + 80, 116, 0);
		} else {
			s = I18n.format("book.pageIndicator", new Object[] {Integer.valueOf(this.currPage + 1), Integer.valueOf(this.bookTotalPages)});
			s1 = "";

			if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount()) {
				s1 = this.bookPages.getStringTagAt(this.currPage);
			}

			if (this.bookIsUnsigned) {
				if (this.fontRenderer.getBidiFlag()) {
					s1 = s1 + "_";
				} else if (this.updateCount / 6 % 2 == 0) {
					s1 = s1 + "" + ChatFormatting.BLACK + "_";
				} else {
					s1 = s1 + "" + ChatFormatting.GRAY + "_";
				}
			}

			l = this.fontRenderer.getStringWidth(s);
			this.fontRenderer.drawString(s, k - l + this.bookImageWidth - 44, b0 + 16, 0);
			this.fontRenderer.drawSplitString(s1, k + 36, b0 + 16 + 16, 116, 0);
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
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
