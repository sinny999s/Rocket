/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.lwjgl.glfw.GLFW
 */
package rich.screens.clickgui.impl.background.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.lwjgl.glfw.GLFW;
import rich.IMinecraft;
import rich.Initialization;
import rich.modules.module.ModuleRepository;
import rich.modules.module.ModuleStructure;

public class SearchHandler
implements IMinecraft {
    private boolean searchActive = false;
    private String searchText = "";
    private int searchCursorPosition = 0;
    private int searchSelectionStart = -1;
    private int searchSelectionEnd = -1;
    private float searchCursorBlink = 0.0f;
    private float searchBoxAnimation = 0.0f;
    private float searchFocusAnimation = 0.0f;
    private float searchPanelAlpha = 0.0f;
    private float normalPanelAlpha = 1.0f;
    private float searchSelectionAnimation = 0.0f;
    private List<ModuleStructure> searchResults = new ArrayList<ModuleStructure>();
    private Map<ModuleStructure, Float> searchResultAnimations = new HashMap<ModuleStructure, Float>();
    private Map<ModuleStructure, Long> searchResultAnimStartTimes = new HashMap<ModuleStructure, Long>();
    private float searchScrollOffset = 0.0f;
    private float searchTargetScroll = 0.0f;
    private int hoveredSearchIndex = -1;
    private ModuleStructure selectedSearchModule = null;
    private static final float SEARCH_ANIM_SPEED = 8.0f;
    private static final float PANEL_FADE_SPEED = 15.0f;
    private static final float SEARCH_RESULT_HEIGHT = 18.0f;
    private static final float SEARCH_RESULT_ANIM_DURATION = 200.0f;

    public void setSearchActive(boolean active) {
        if (active && !this.searchActive) {
            this.searchText = "";
            this.searchCursorPosition = 0;
            this.searchSelectionStart = -1;
            this.searchSelectionEnd = -1;
            this.searchResults.clear();
            this.searchResultAnimations.clear();
            this.searchResultAnimStartTimes.clear();
            this.searchScrollOffset = 0.0f;
            this.searchTargetScroll = 0.0f;
            this.hoveredSearchIndex = -1;
            this.selectedSearchModule = null;
        }
        this.searchActive = active;
    }

    public void updateAnimations(float deltaTime) {
        float searchTarget = this.searchActive ? 1.0f : 0.0f;
        this.searchBoxAnimation = this.updateAnimation(this.searchBoxAnimation, searchTarget, 8.0f, deltaTime);
        this.searchFocusAnimation = this.updateAnimation(this.searchFocusAnimation, searchTarget, 8.0f, deltaTime);
        this.searchPanelAlpha = this.updateAnimation(this.searchPanelAlpha, searchTarget, 15.0f, deltaTime);
        this.normalPanelAlpha = this.updateAnimation(this.normalPanelAlpha, this.searchActive ? 0.0f : 1.0f, 15.0f, deltaTime);
        this.searchSelectionAnimation = this.updateAnimation(this.searchSelectionAnimation, this.hasSearchSelection() ? 1.0f : 0.0f, 8.0f, deltaTime);
        if (this.searchActive) {
            this.searchCursorBlink += deltaTime * 2.0f;
            if (this.searchCursorBlink > 1.0f) {
                this.searchCursorBlink -= 1.0f;
            }
        }
        this.updateResultAnimations();
        this.updateScrollAnimation(deltaTime);
    }

    private float updateAnimation(float current, float target, float speed, float deltaTime) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001f) {
            return target;
        }
        return current + diff * speed * deltaTime;
    }

    private void updateResultAnimations() {
        long currentTime = System.currentTimeMillis();
        for (ModuleStructure mod : this.searchResults) {
            Long startTime = this.searchResultAnimStartTimes.get(mod);
            if (startTime == null) continue;
            float elapsed = currentTime - startTime;
            float progress = Math.min(1.0f, Math.max(0.0f, elapsed / 200.0f));
            progress = this.easeOutCubic(progress);
            this.searchResultAnimations.put(mod, Float.valueOf(progress));
        }
    }

    private void updateScrollAnimation(float deltaTime) {
        float scrollDiff = this.searchTargetScroll - this.searchScrollOffset;
        this.searchScrollOffset = Math.abs(scrollDiff) < 0.5f ? this.searchTargetScroll : (this.searchScrollOffset += scrollDiff * 12.0f * deltaTime);
    }

    private float easeOutCubic(float x) {
        return 1.0f - (float)Math.pow(1.0f - x, 3.0);
    }

    public boolean hasSearchSelection() {
        return this.searchSelectionStart != -1 && this.searchSelectionEnd != -1 && this.searchSelectionStart != this.searchSelectionEnd;
    }

    public int getSearchSelectionStart() {
        return Math.min(this.searchSelectionStart, this.searchSelectionEnd);
    }

    public int getSearchSelectionEnd() {
        return Math.max(this.searchSelectionStart, this.searchSelectionEnd);
    }

    private void clearSearchSelection() {
        this.searchSelectionStart = -1;
        this.searchSelectionEnd = -1;
    }

    private void selectAllSearchText() {
        this.searchSelectionStart = 0;
        this.searchSelectionEnd = this.searchText.length();
        this.searchCursorPosition = this.searchText.length();
    }

    private void deleteSelectedSearchText() {
        if (this.hasSearchSelection()) {
            int start = this.getSearchSelectionStart();
            int end = this.getSearchSelectionEnd();
            this.searchText = this.searchText.substring(0, start) + this.searchText.substring(end);
            this.searchCursorPosition = start;
            this.clearSearchSelection();
            this.updateSearchResults();
        }
    }

    private String getSelectedSearchText() {
        if (!this.hasSearchSelection()) {
            return "";
        }
        return this.searchText.substring(this.getSearchSelectionStart(), this.getSearchSelectionEnd());
    }

    private void copySearchToClipboard() {
        if (this.hasSearchSelection()) {
            GLFW.glfwSetClipboardString((long)mc.getWindow().handle(), (CharSequence)this.getSelectedSearchText());
        }
    }

    private void pasteToSearch() {
        String clipboardText = GLFW.glfwGetClipboardString((long)mc.getWindow().handle());
        if (clipboardText != null && !clipboardText.isEmpty()) {
            clipboardText = clipboardText.replaceAll("[\n\r\t]", "");
            if (this.hasSearchSelection()) {
                this.deleteSelectedSearchText();
            }
            this.searchText = this.searchText.substring(0, this.searchCursorPosition) + clipboardText + this.searchText.substring(this.searchCursorPosition);
            this.searchCursorPosition += clipboardText.length();
            this.updateSearchResults();
        }
    }

    private boolean isControlDown() {
        long window = mc.getWindow().handle();
        return GLFW.glfwGetKey((long)window, (int)341) == 1 || GLFW.glfwGetKey((long)window, (int)345) == 1;
    }

    private boolean isShiftDown() {
        long window = mc.getWindow().handle();
        return GLFW.glfwGetKey((long)window, (int)340) == 1 || GLFW.glfwGetKey((long)window, (int)344) == 1;
    }

    public void updateSearchResults() {
        if (this.searchText.isEmpty()) {
            this.searchResults.clear();
            this.searchResultAnimations.clear();
            this.searchResultAnimStartTimes.clear();
            this.searchScrollOffset = 0.0f;
            this.searchTargetScroll = 0.0f;
            this.selectedSearchModule = null;
            return;
        }
        String query = this.searchText.toLowerCase();
        ArrayList<ModuleStructure> newResults = new ArrayList<ModuleStructure>();
        HashMap<ModuleStructure, Float> oldAnimations = new HashMap<ModuleStructure, Float>(this.searchResultAnimations);
        try {
            ModuleRepository repo = Initialization.getInstance().getManager().getModuleRepository();
            if (repo != null) {
                for (ModuleStructure module : repo.modules()) {
                    if (!module.getName().toLowerCase().contains(query)) continue;
                    newResults.add(module);
                }
            }
        }
        catch (Exception repo) {
            // empty catch block
        }
        this.searchResultAnimations.clear();
        this.searchResultAnimStartTimes.clear();
        long currentTime = System.currentTimeMillis();
        int newIndex = 0;
        for (int i = 0; i < newResults.size(); ++i) {
            ModuleStructure module = (ModuleStructure)newResults.get(i);
            if (oldAnimations.containsKey(module)) {
                float oldProgress = ((Float)oldAnimations.get(module)).floatValue();
                this.searchResultAnimations.put(module, Float.valueOf(Math.max(oldProgress, 0.5f)));
                this.searchResultAnimStartTimes.put(module, currentTime - 170L);
                continue;
            }
            this.searchResultAnimations.put(module, Float.valueOf(0.0f));
            this.searchResultAnimStartTimes.put(module, currentTime + (long)newIndex * 40L);
            ++newIndex;
        }
        this.searchResults = newResults;
        if (!this.searchResults.isEmpty()) {
            if (this.selectedSearchModule == null || !this.searchResults.contains(this.selectedSearchModule)) {
                this.selectedSearchModule = this.searchResults.get(0);
            }
        } else {
            this.selectedSearchModule = null;
        }
    }

    public boolean handleSearchChar(char chr) {
        if (!this.searchActive) {
            return false;
        }
        if (Character.isISOControl(chr)) {
            return false;
        }
        if (this.hasSearchSelection()) {
            this.deleteSelectedSearchText();
        }
        this.searchText = this.searchText.substring(0, this.searchCursorPosition) + chr + this.searchText.substring(this.searchCursorPosition);
        ++this.searchCursorPosition;
        this.clearSearchSelection();
        this.updateSearchResults();
        return true;
    }

    public boolean handleSearchKey(int keyCode) {
        if (!this.searchActive) {
            return false;
        }
        if (this.isControlDown()) {
            switch (keyCode) {
                case 65: {
                    this.selectAllSearchText();
                    return true;
                }
                case 67: {
                    this.copySearchToClipboard();
                    return true;
                }
                case 86: {
                    this.pasteToSearch();
                    return true;
                }
                case 88: {
                    if (this.hasSearchSelection()) {
                        this.copySearchToClipboard();
                        this.deleteSelectedSearchText();
                    }
                    return true;
                }
            }
        }
        switch (keyCode) {
            case 259: {
                if (this.hasSearchSelection()) {
                    this.deleteSelectedSearchText();
                } else if (this.searchCursorPosition > 0) {
                    this.searchText = this.searchText.substring(0, this.searchCursorPosition - 1) + this.searchText.substring(this.searchCursorPosition);
                    --this.searchCursorPosition;
                    this.updateSearchResults();
                }
                return true;
            }
            case 261: {
                if (this.hasSearchSelection()) {
                    this.deleteSelectedSearchText();
                } else if (this.searchCursorPosition < this.searchText.length()) {
                    this.searchText = this.searchText.substring(0, this.searchCursorPosition) + this.searchText.substring(this.searchCursorPosition + 1);
                    this.updateSearchResults();
                }
                return true;
            }
            case 263: {
                this.handleLeftKey();
                return true;
            }
            case 262: {
                this.handleRightKey();
                return true;
            }
            case 268: {
                this.handleHomeKey();
                return true;
            }
            case 269: {
                this.handleEndKey();
                return true;
            }
            case 265: {
                int currentIndex;
                if (!this.searchResults.isEmpty() && this.selectedSearchModule != null && (currentIndex = this.searchResults.indexOf(this.selectedSearchModule)) > 0) {
                    this.selectedSearchModule = this.searchResults.get(currentIndex - 1);
                }
                return true;
            }
            case 264: {
                int currentIndex;
                if (!this.searchResults.isEmpty() && this.selectedSearchModule != null && (currentIndex = this.searchResults.indexOf(this.selectedSearchModule)) < this.searchResults.size() - 1) {
                    this.selectedSearchModule = this.searchResults.get(currentIndex + 1);
                }
                return true;
            }
            case 257: {
                if (this.selectedSearchModule != null) {
                    this.selectedSearchModule.switchState();
                }
                return true;
            }
            case 256: {
                this.setSearchActive(false);
                return true;
            }
        }
        return false;
    }

    private void handleLeftKey() {
        if (this.hasSearchSelection() && !this.isShiftDown()) {
            this.searchCursorPosition = this.getSearchSelectionStart();
            this.clearSearchSelection();
        } else if (this.searchCursorPosition > 0) {
            if (this.isShiftDown()) {
                if (this.searchSelectionStart == -1) {
                    this.searchSelectionStart = this.searchCursorPosition;
                }
                --this.searchCursorPosition;
                this.searchSelectionEnd = this.searchCursorPosition;
            } else {
                --this.searchCursorPosition;
                this.clearSearchSelection();
            }
        }
    }

    private void handleRightKey() {
        if (this.hasSearchSelection() && !this.isShiftDown()) {
            this.searchCursorPosition = this.getSearchSelectionEnd();
            this.clearSearchSelection();
        } else if (this.searchCursorPosition < this.searchText.length()) {
            if (this.isShiftDown()) {
                if (this.searchSelectionStart == -1) {
                    this.searchSelectionStart = this.searchCursorPosition;
                }
                ++this.searchCursorPosition;
                this.searchSelectionEnd = this.searchCursorPosition;
            } else {
                ++this.searchCursorPosition;
                this.clearSearchSelection();
            }
        }
    }

    private void handleHomeKey() {
        if (this.isShiftDown()) {
            if (this.searchSelectionStart == -1) {
                this.searchSelectionStart = this.searchCursorPosition;
            }
            this.searchSelectionEnd = this.searchCursorPosition = 0;
        } else {
            this.searchCursorPosition = 0;
            this.clearSearchSelection();
        }
    }

    private void handleEndKey() {
        if (this.isShiftDown()) {
            if (this.searchSelectionStart == -1) {
                this.searchSelectionStart = this.searchCursorPosition;
            }
            this.searchSelectionEnd = this.searchCursorPosition = this.searchText.length();
        } else {
            this.searchCursorPosition = this.searchText.length();
            this.clearSearchSelection();
        }
    }

    public void handleSearchScroll(double vertical, float panelHeight) {
        if (!this.searchActive || this.searchResults.isEmpty()) {
            return;
        }
        float maxScroll = Math.max(0.0f, (float)this.searchResults.size() * 20.0f - panelHeight + 10.0f);
        this.searchTargetScroll = (float)Math.max((double)(-maxScroll), Math.min(0.0, (double)this.searchTargetScroll + vertical * 25.0));
    }

    public float getSearchResultHeight() {
        return 18.0f;
    }

    public void setHoveredSearchIndex(int index) {
        this.hoveredSearchIndex = index;
    }

    @Generated
    public boolean isSearchActive() {
        return this.searchActive;
    }

    @Generated
    public String getSearchText() {
        return this.searchText;
    }

    @Generated
    public int getSearchCursorPosition() {
        return this.searchCursorPosition;
    }

    @Generated
    public float getSearchCursorBlink() {
        return this.searchCursorBlink;
    }

    @Generated
    public float getSearchBoxAnimation() {
        return this.searchBoxAnimation;
    }

    @Generated
    public float getSearchFocusAnimation() {
        return this.searchFocusAnimation;
    }

    @Generated
    public float getSearchPanelAlpha() {
        return this.searchPanelAlpha;
    }

    @Generated
    public float getNormalPanelAlpha() {
        return this.normalPanelAlpha;
    }

    @Generated
    public float getSearchSelectionAnimation() {
        return this.searchSelectionAnimation;
    }

    @Generated
    public List<ModuleStructure> getSearchResults() {
        return this.searchResults;
    }

    @Generated
    public Map<ModuleStructure, Float> getSearchResultAnimations() {
        return this.searchResultAnimations;
    }

    @Generated
    public Map<ModuleStructure, Long> getSearchResultAnimStartTimes() {
        return this.searchResultAnimStartTimes;
    }

    @Generated
    public float getSearchScrollOffset() {
        return this.searchScrollOffset;
    }

    @Generated
    public float getSearchTargetScroll() {
        return this.searchTargetScroll;
    }

    @Generated
    public int getHoveredSearchIndex() {
        return this.hoveredSearchIndex;
    }

    @Generated
    public ModuleStructure getSelectedSearchModule() {
        return this.selectedSearchModule;
    }
}

