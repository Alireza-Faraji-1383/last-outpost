package dev.exodus.supply.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.exodus.supply.menu.SupplyRadioMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SupplyRadioScreen extends AbstractContainerScreen<SupplyRadioMenu> {
    public SupplyRadioScreen(SupplyRadioMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=248;imageHeight=210;}
    @Override protected void init(){super.init();int y=topPos+28;for(int i=0;i<menu.entries().size()&&i<6;i++){var e=menu.entries().get(i);int button=i;addRenderableWidget(Button.builder(Component.literal("Request"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,button)).bounds(leftPos+170,y,64,20).build());y+=28;}}
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xE0101820);int y=topPos+33;for(int i=0;i<menu.entries().size()&&i<6;i++){var e=menu.entries().get(i);g.drawString(font,e.name(),leftPos+12,y,0xFFFFFF,false);g.drawString(font,e.cost()+" | "+(e.remaining()<0?"Unlimited":e.remaining()+" left"),leftPos+12,y+10,0xAAAAAA,false);y+=28;}}
    @Override public void render(GuiGraphics g,int x,int y,float p){renderBackground(g);super.render(g,x,y,p);renderTooltip(g,x,y);}
}
