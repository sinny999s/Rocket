
package rich.util.modules.esp;

public class RwPrefix {
    public static String getIconLabel(char c) {
        return switch (c) {
            case '\ua500' -> "PLAYER";
            case '\ua504' -> "HERO";
            case '\ua508' -> "TITAN";
            case '\ua512' -> "AVENGER";
            case '\ua516' -> "OVERLORD";
            case '\ua520' -> "MAGISTER";
            case '\ua524' -> "IMPERATOR";
            case '\ua528' -> "DRAGON";
            case '\ua532' -> "BULL";
            case '\ua552' -> "RABBIT";
            case '\ua536' -> "TIGER";
            case '\ua544' -> "DRACULA";
            case '\ua556' -> "BUNNY";
            case '\ua540' -> "HYDRA";
            case '\ua548' -> "COBRA";
            case '\ua501' -> "MEDIA";
            case '\ua505' -> "YT";
            case '\ua560' -> "D.HELPER";
            case '\ua509' -> "HELPER";
            case '\ua513' -> "ML.MODER";
            case '\ua517' -> "MODER";
            case '\ua521' -> "MODER+";
            case '\ua525' -> "ST.MODER";
            case '\ua529' -> "GL.MODER";
            case '\ua533' -> "ML.ADMIN";
            case '\ua537' -> "ADMIN";
            case '\ua545' -> "VAMPIRE";
            case '\ua549' -> "PEGAS";
            default -> null;
        };
    }

    public static boolean isIcon(char c) {
        if (RwPrefix.getIconLabel(c) != null) {
            return true;
        }
        return c >= '\ua000' && c <= '\uafff' || c >= '\ue000' && c <= '\uf8ff' || c >= '\u2400' && c <= '\u243f' || c >= '\u2500' && c <= '\u257f';
    }

    public static String stripFormatting(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c == '\u00a7' && i + 1 < text.length()) {
                char next = text.charAt(i + 1);
                if (next == '#' && i + 7 < text.length()) {
                    i += 7;
                    continue;
                }
                if ((next == 'x' || next == 'X') && i + 13 < text.length()) {
                    i += 13;
                    continue;
                }
                ++i;
                continue;
            }
            result.append(c);
        }
        return result.toString();
    }

    public static ParsedName parseDisplayName(String displayName) {
        int spaceIdx;
        String possibleClan;
        if (displayName == null || displayName.isEmpty()) {
            return new ParsedName("", "", "");
        }
        String clean = RwPrefix.stripFormatting(displayName);
        StringBuilder prefix = new StringBuilder();
        StringBuilder name = new StringBuilder();
        StringBuilder clan = new StringBuilder();
        boolean foundName = false;
        boolean inClan = false;
        int clanBracketCount = 0;
        for (int i = 0; i < clean.length(); ++i) {
            char c = clean.charAt(i);
            if (RwPrefix.isIcon(c)) {
                String label = RwPrefix.getIconLabel(c);
                if (label == null) continue;
                if (prefix.length() > 0) {
                    prefix.append(" ");
                }
                prefix.append(label);
                continue;
            }
            if (!foundName && (c == ' ' || c == '[' || c == ']')) continue;
            if (!foundName && Character.isLetterOrDigit(c) || c == '_') {
                foundName = true;
            }
            if (!foundName) continue;
            if (c == '[') {
                inClan = true;
                ++clanBracketCount;
                clan.append(c);
                continue;
            }
            if (c == ']' && inClan) {
                clan.append(c);
                if (--clanBracketCount > 0) continue;
                inClan = false;
                continue;
            }
            if (inClan) {
                clan.append(c);
                continue;
            }
            if (c == ' ' && name.length() <= 0 || c == ' ' && i + 1 < clean.length() && clean.charAt(i + 1) == '[' || inClan || clan.length() != 0) continue;
            name.append(c);
        }
        String nameStr = name.toString().trim();
        if (nameStr.contains(" ") && (possibleClan = nameStr.substring(spaceIdx = nameStr.indexOf(32)).trim()).startsWith("[") && possibleClan.endsWith("]")) {
            clan = new StringBuilder(possibleClan);
            nameStr = nameStr.substring(0, spaceIdx);
        }
        return new ParsedName(prefix.toString().trim(), nameStr.trim(), clan.toString().trim());
    }

    public static class ParsedName {
        public final String prefix;
        public final String name;
        public final String clan;

        public ParsedName(String prefix, String name, String clan) {
            this.prefix = prefix;
            this.name = name;
            this.clan = clan;
        }

        public String getFullText() {
            StringBuilder sb = new StringBuilder();
            if (!this.prefix.isEmpty()) {
                sb.append(this.prefix).append(" ");
            }
            sb.append(this.name);
            if (!this.clan.isEmpty()) {
                sb.append(" ").append(this.clan);
            }
            return sb.toString();
        }
    }
}

