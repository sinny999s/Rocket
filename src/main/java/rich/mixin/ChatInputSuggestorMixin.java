/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ParseResults
 *  com.mojang.brigadier.context.StringRange
 *  com.mojang.brigadier.suggestion.Suggestion
 *  com.mojang.brigadier.suggestion.Suggestions
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package rich.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.TabCompleteEvent;

@Mixin(value={CommandSuggestions.class})
public abstract class ChatInputSuggestorMixin {
    @Shadow
    @Final
    EditBox input;
    @Shadow
    @Final
    private List<FormattedCharSequence> commandUsage;
    @Shadow
    private ParseResults<?> currentParse;
    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow
    private CommandSuggestions.SuggestionsList suggestions;
    @Shadow
    boolean keepSuggestions;

    @Shadow
    public abstract void showSuggestions(boolean var1);

    @Inject(method={"updateCommandInfo"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRefresh(CallbackInfo ci) {
        String text = this.input.getValue();
        int cursor = this.input.getCursorPosition();
        String prefix = text.substring(0, Math.min(text.length(), cursor));
        TabCompleteEvent event = new TabCompleteEvent(prefix);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
            return;
        }
        if (event.completions != null) {
            ci.cancel();
            this.currentParse = null;
            if (this.keepSuggestions) {
                return;
            }
            this.input.setSuggestion(null);
            this.suggestions = null;
            this.commandUsage.clear();
            if (event.completions.length == 0) {
                this.pendingSuggestions = Suggestions.empty();
            } else {
                int lastSpace = prefix.lastIndexOf(32);
                StringRange range = StringRange.between((int)(lastSpace + 1), (int)prefix.length());
                List suggestionList = Stream.of(event.completions).map(s -> new Suggestion(range, s)).collect(Collectors.toList());
                Suggestions suggestions = new Suggestions(range, suggestionList);
                this.pendingSuggestions = new CompletableFuture();
                this.pendingSuggestions.complete(suggestions);
            }
            this.showSuggestions(true);
        }
    }
}

