package dev.exodus.supply.client;

import dev.exodus.supply.menu.SupplyRadioMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SupplyRadioScreen extends AbstractContainerScreen<SupplyRadioMenu> {
    private static final int PAGE_SIZE=5, ROW_HEIGHT=34;
    private int page;
    public SupplyRadioScreen(SupplyRadioMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=320;imageHeight=228;inventoryLabelY=-1000;}
    @Override protected void init(){super.init();createButtons();}
    private int pageCount(){return Math.max(1,(menu.entries().size()+PAGE_SIZE-1)/PAGE_SIZE);}
    private void createButtons(){
        clearWidgets(); int start=page*PAGE_SIZE;
        for(int row=0;row<PAGE_SIZE&&start+row<menu.entries().size();row++){
            int index=start+row;var entry=menu.entries().get(index);
            Button button=Button.builder(Component.literal("Request"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,index)).bounds(leftPos+244,topPos+27+row*ROW_HEIGHT,64,20).build();
            button.active=entry.remaining()!=0; addRenderableWidget(button);
        }
        addRenderableWidget(Button.builder(Component.literal("Previous"),b->{page=Math.max(0,page-1);createButtons();}).bounds(leftPos+12,topPos+imageHeight-27,80,20).build());
        addRenderableWidget(Button.builder(Component.literal("Next"),b->{page=Math.min(pageCount()-1,page+1);createButtons();}).bounds(leftPos+imageWidth-92,topPos+imageHeight-27,80,20).build());
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY){
        g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xF0101820);int start=page*PAGE_SIZE;
        for(int row=0;row<PAGE_SIZE&&start+row<menu.entries().size();row++){
            var e=menu.entries().get(start+row);int y=topPos+28+row*ROW_HEIGHT;
            g.drawString(font,font.plainSubstrByWidth(e.name(),225),leftPos+12,y,0xFFFFFF,false);
            g.drawString(font,e.cost()+" | "+(e.remaining()<0?"Unlimited":e.remaining()+" left"),leftPos+12,y+11,0xAAAAAA,false);
            g.drawString(font,"Hover for contents",leftPos+12,y+22,0x6C92AE,false);
        }
        g.drawCenteredString(font,(page+1)+" / "+pageCount(),leftPos+imageWidth/2,topPos+imageHeight-21,0xFFFFFF);
    }
    @Override public void render(GuiGraphics g,int x,int y,float p){
        renderBackground(g);super.render(g,x,y,p);
        int row=(y-topPos-27)/ROW_HEIGHT,index=page*PAGE_SIZE+row;
        if(x>=leftPos+10&&x<leftPos+240&&y>=topPos+27&&row>=0&&row<PAGE_SIZE&&index<menu.entries().size()){
            var e=menu.entries().get(index);String text=e.contents().isBlank()?e.name():e.contents();
            g.renderTooltip(font,font.split(Component.literal(text),250),x,y);
        }
    }
}
