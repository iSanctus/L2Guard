package net.sf.l2j.gameserver.util.sellBuffEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
/*
L2jbrasil Dhousefe
*/

public class BuffShopConfigs
{
    private static final String BUFF_SHOP_CONFIG_FILE = "./config/CustomMods/SpecialMods.ini";
    private static final Logger _log = Logger.getLogger(BuffShopConfigs.class.getName());
    
    public static List<Integer> BUFFSHOP_ALLOW_CLASS = new ArrayList<>();
    public static List<Integer> BUFFSHOP_FORBIDDEN_SKILL = new ArrayList<>();
    public static List<Integer> BUFFSHOP_ALLOWED_SELF_SKILLS = new ArrayList<>();
    public static List<Integer> BUFFSHOP_RESTRICTED_SUMMONS = new ArrayList<>();
    public static List<Integer> NON_REMOVABLE_BUFFS = new ArrayList<>();
    public static Set<ClassId> BUFFSHOP_SUMMON_BUYER_CLASSES = EnumSet.noneOf(ClassId.class);
    public static record SkillGrantRule(int requiredLevel, int skillId, int skillLevel) {}
    public static List<Integer> BUFFSHOP_REPLACEABLE_BUFFS = new ArrayList<>();

    // --- Registros para Tipos de Dados Fortes ---
    public record Cost(int itemId, int count) {}
    public record SkillPath(int maxLevel, Map<Integer, List<Cost>> costsByLevel) {}

    // --- Vari�veis de Configura��o ---
    public static final List<Integer> BUFFSHOP_ALLOW_CLASS_SKILLSHOP = new ArrayList<>();
    public static final List<Integer> SKILL_SHOP_ALLOWED_CLASSES = new ArrayList<>();
    public static final Map<ClassId, List<Integer>> SKILL_SHOP_AVAILABLE = new HashMap<>();
    public static final Map<Integer, SkillPath> SKILL_SHOP_PATHS = new HashMap<>();
    public static int SKILL_SHOP_REQUIRED_ITEM_ID;


    
    // O mapa agora armazena uma lista de regras para cada ClassId.
    public static final Map<ClassId, List<SkillGrantRule>> BUFFSHOP_GRANT_SKILLS = new HashMap<>();

    public static final Map<String, List<Integer>> BUFFSHOP_CLASS_SPECIFIC_SKILLS = new HashMap<>();
    public static int BUFFSHOP_BUFFS_MAX_COUNT = 8; // Valor padr�o
    private final Properties DhousefeBuffProperties = new Properties();
    // Lista de classes de Summoner que ser�o usadas como refer�ncia para verificar o n�vel de skill.
    public static final List<ClassId> REFERENCE_SUMMONER_CLASSES = List.of(
        ClassId.WARLOCK,
        ClassId.ELEMENTAL_SUMMONER,
        ClassId.PHANTOM_SUMMONER
    );
    
    
    
    public void loadConfigs()
    {

        // Limpa as listas para o caso de um reload
        BUFFSHOP_ALLOW_CLASS_SKILLSHOP.clear();
        SKILL_SHOP_ALLOWED_CLASSES.clear();
        SKILL_SHOP_AVAILABLE.clear();
        SKILL_SHOP_PATHS.clear();

        SKILL_SHOP_REQUIRED_ITEM_ID = Integer.parseInt(DhousefeBuffProperties.getProperty("SkillShopRequiredItemId", "6622"));


        // Load BuffShop L2Properties file (if exists)
        final File l2dsecsellbuff = new File(BUFF_SHOP_CONFIG_FILE);
        if (!l2dsecsellbuff.exists())
        {
        	_log.log(Level.SEVERE, "BuffShop System: Configuration file not found: " + BUFF_SHOP_CONFIG_FILE + ". Using default values.");
            return; // Aborta o carregamento
        }
        try (InputStream is = new FileInputStream(l2dsecsellbuff))
        {
            DhousefeBuffProperties.load(is);
        }
        catch (Exception e)
        {
        	_log.log(Level.SEVERE, "Error while loading BuffShop settings!", e);
            return; // Aborta o carregamento
        }
        
        try
        {
            BUFFSHOP_BUFFS_MAX_COUNT = Integer.parseInt(DhousefeBuffProperties.getProperty("MaxCount", "8"));
        }
        catch (NumberFormatException e)
        {
        	_log.log(Level.SEVERE, "BuffShop System: Error parsing MaxCount. Using default value 8.");
            BUFFSHOP_BUFFS_MAX_COUNT = 8;
        }
        
        String[] classIds = DhousefeBuffProperties.getProperty("BuffShopAllowClassId", "").split(",");
        for (String id : classIds)
        {
            try
            {
                BUFFSHOP_ALLOW_CLASS.add(Integer.parseInt(id.trim()));
            }
            catch (NumberFormatException e)
            {
                _log.info("BuffShop System: Error parsing Class id '" + id + "' from property 'BuffShopAllowClassId'. Skipping.");
            }
        }
        
        String[] skillIds = DhousefeBuffProperties.getProperty("BuffShopForbiddenSkill", "").split(",");
        for (String id : skillIds)
        {
            try
            {
                BUFFSHOP_FORBIDDEN_SKILL.add(Integer.parseInt(id.trim()));
            }
            catch (NumberFormatException e)
            {
                _log.info("BuffShop System: Error parsing Skill id '" + id + "' from property 'BuffShopForbiddenSkill'. Skipping.");
            }
        }
        String[] allowedSelfSkillIds = DhousefeBuffProperties.getProperty("BuffShopAllowedSelfSkill", "").split(",");
        for (String id : allowedSelfSkillIds) {
            if (id.isEmpty()) {
                continue;
            }
            try {
                BUFFSHOP_ALLOWED_SELF_SKILLS.add(Integer.parseInt(id.trim()));
            } catch (NumberFormatException e) {
            	_log.log(Level.SEVERE, "BuffShop System: Error parsing Skill id '{}' from property 'BuffShopAllowedSelfSkill'. Skipping.", id);
            }
        }

        BUFFSHOP_GRANT_SKILLS.clear();
        
        final String grantPrefix = "GrantSkillsOnManage.";
        
        for (String key : DhousefeBuffProperties.stringPropertyNames()) {
            if (key.startsWith(grantPrefix)) {
                try {
                    String className = key.substring(grantPrefix.length()).toUpperCase();
                    ClassId classIdKey = ClassId.valueOf(className);
                    
                    String value = DhousefeBuffProperties.getProperty(key, "");
                    if (value.isEmpty()) continue;
                    
                    List<SkillGrantRule> rules = new ArrayList<>();
                    
                    // Analisa a string de regras (ex: "40:10:1,44:10:2,...")
                    for (String ruleString : value.split(",")) {
                        ruleString = ruleString.trim();
                        if (ruleString.isEmpty()) continue;
                        
                        String[] ruleParts = ruleString.split(":");
                        if (ruleParts.length == 3) {
                            int reqLevel = Integer.parseInt(ruleParts[0]);
                            int skillId = Integer.parseInt(ruleParts[1]);
                            int skillLevel = Integer.parseInt(ruleParts[2]);
                            rules.add(new SkillGrantRule(reqLevel, skillId, skillLevel));
                        }
                    }
                    
                    if (!rules.isEmpty()) {
                        BUFFSHOP_GRANT_SKILLS.put(classIdKey, rules);
                        _log.log(Level.SEVERE, "BuffShop System: Loaded {} skill grant rules for class {}.", className);
                    }
                } catch (Exception e) {
                	_log.log(Level.SEVERE, "BuffShop System: Error parsing property '{}'. Please check format (LvlReq:SkillID:SkillLvl,...).", e);
                }
            }
        }

        // Limpa o mapa antes de carregar para o caso de um reload de configs.
        BUFFSHOP_CLASS_SPECIFIC_SKILLS.clear();
        
        // Itera sobre todas as propriedades carregadas do arquivo .ini
        for (String key : DhousefeBuffProperties.stringPropertyNames()) {
            // Verifica se a propriedade corresponde ao nosso padr�o para skills de classe
            if (key.startsWith("ClassSpecificSkills.")) {
                try {
                    // Extrai o nome da classe da chave (ex: "WARLOCK" de "ClassSpecificSkills.WARLOCK")
                    String className = key.substring("ClassSpecificSkills.".length());
                    
                    // Pega o valor (a lista de IDs de skill separada por v�rgula)
                    String value = DhousefeBuffProperties.getProperty(key, "");
                    
                    // Cria uma nova lista para armazenar os IDs das skills
                    List<Integer> classSkillIds = new ArrayList<>();
                    
                    // Analisa a string de IDs
                    for (String idStr : value.split(",")) {
                        if (!idStr.trim().isEmpty()) {
                            classSkillIds.add(Integer.parseInt(idStr.trim()));
                        }
                    }
                    
                    // Armazena a lista de IDs de skill associada ao nome da classe
                    if (!classSkillIds.isEmpty()) {
                        BUFFSHOP_CLASS_SPECIFIC_SKILLS.put(className, classSkillIds);
                        _log.log(Level.SEVERE, "BuffShop System: Loaded {} specific skills for class {}.", classSkillIds.size());
                    }
                } catch (Exception e) {
                	_log.log(Level.SEVERE, "BuffShop System: Error parsing property '{}'. Please check the format.", e);
                }
            }
        }

        String[] restrictedSummonIds = DhousefeBuffProperties.getProperty("BuffShopRestrictedSummons", "").split(",");
        for (String id : restrictedSummonIds) {
            if (!id.trim().isEmpty()) {
                try {
                    BUFFSHOP_RESTRICTED_SUMMONS.add(Integer.parseInt(id.trim()));
                } catch (NumberFormatException e) {
                	_log.log(Level.SEVERE, "BuffShop System: Error parsing Skill id '{}' from property 'BuffShopRestrictedSummons'.", id);
                }
            }
        }
        
        BUFFSHOP_SUMMON_BUYER_CLASSES.clear();
        
        // Pega a propriedade como uma string de nomes de classe separados por v�rgula.
        String[] summonBuyerClassNames = DhousefeBuffProperties.getProperty("BuffShopSummonBuyerClasses", "").split(",");
        for (String className : summonBuyerClassNames) {
            className = className.trim(); // Limpa espa�os em branco
            if (className.isEmpty()) {
                continue;
            }
            try {
                // Converte o NOME da string para o ENUM ClassId correspondente.
                ClassId classId = ClassId.valueOf(className);
                BUFFSHOP_SUMMON_BUYER_CLASSES.add(classId);
            } catch (IllegalArgumentException e) {
                // Este erro acontece se o nome da classe no .ini estiver escrito errado.
            	_log.log(Level.SEVERE, "BuffShop System: BUFFSHOP_SUMMON_BUYER_CLASSES=CLASSNAME '{}' from property 'net.sf.l2j.gameserver.enums.actors.ClassId' is invalid. Skipping.", className);
            }
        }

        String[] nonRemovableIds = DhousefeBuffProperties.getProperty("NonRemovableBuffs", "").split(",");
        for (String id : nonRemovableIds) {
            if (!id.trim().isEmpty()) {
                try {
                    NON_REMOVABLE_BUFFS.add(Integer.parseInt(id.trim()));
                } catch (NumberFormatException e) {
                	_log.log(Level.SEVERE, "BuffShop System: Error parsing Skill id '{}' from property 'NonRemovableBuffs'.", id);
                }
            }
        }

        String[] replaceableIds = DhousefeBuffProperties.getProperty("BuffShopReplaceableBuffs", "").split(",");
        for (String id : replaceableIds) {
            if (!id.trim().isEmpty()) {
                try {
                    BUFFSHOP_REPLACEABLE_BUFFS.add(Integer.parseInt(id.trim()));
                } catch (NumberFormatException e) {
                	_log.log(Level.SEVERE, "BuffShop System: Error parsing Skill id '{}' from property 'BuffShopReplaceableBuffs'.", id);
                }
            }
        }

        // --- Carrega as Novas Configura��es da Loja de Skills ---
        String[] allowedSkillShop = DhousefeBuffProperties.getProperty("SkillShopAllowedClasses", "").split(",");
        for (String id : allowedSkillShop) {
            if (!id.trim().isEmpty()) BUFFSHOP_ALLOW_CLASS_SKILLSHOP.add(Integer.parseInt(id.trim()));
        }

        for (String key : DhousefeBuffProperties.stringPropertyNames()) {
            if (key.startsWith("SkillShopAvailable.")) {
                try {
                    ClassId classId = ClassId.valueOf(key.substring("SkillShopAvailable.".length()));
                    List<Integer> skills = new ArrayList<>();
                    for (String skillId : DhousefeBuffProperties.getProperty(key, "").split(",")) {
                        if (!skillId.trim().isEmpty()) skills.add(Integer.parseInt(skillId.trim()));
                    }
                    SKILL_SHOP_AVAILABLE.put(classId, skills);
                } catch (Exception e) { _log.log(Level.SEVERE, "BuffShop: Error parsing property '{}'.", e); }
            } else if (key.startsWith("SkillShopPath.")) {
                try {
                    int skillId = Integer.parseInt(key.substring("SkillShopPath.".length()));
                    String[] parts = DhousefeBuffProperties.getProperty(key, "").split(";");
                    int maxLevel = Integer.parseInt(parts[0].trim());
                    Map<Integer, List<Cost>> costs = new HashMap<>();
                    for (int i = 1; i < parts.length; i++) {
                        List<Cost> levelCost = new ArrayList<>();
                        for (String costStr : parts[i].split(",")) {
                            String[] costParts = costStr.split(":");
                            levelCost.add(new Cost(Integer.parseInt(costParts[0].trim()), Integer.parseInt(costParts[1].trim())));
                        }
                        costs.put(i, levelCost); // i � o n�vel (1, 2, 3...)
                    }
                    SKILL_SHOP_PATHS.put(skillId, new SkillPath(maxLevel, costs));
                } catch (Exception e) { _log.log(Level.SEVERE, "BuffShop: Error parsing property '{}'.", e); }
            }
        }
    

		//restoreOfflineTraders();
    }
    
    public static BuffShopConfigs getInstance()
    {
        return SingletonHolder._instance;
    }
    
    public static void restoreOfflineTraders()
    {
        BuffShopManager.getInstance().restoreOfflineTraders();
    }
    
    private static class SingletonHolder
    {
        protected static final BuffShopConfigs _instance = new BuffShopConfigs();
    }

    public record BuffShopConfig(
    List<Integer> allowedClasses,
    List<Integer> forbiddenSkills,
    List<Integer> allowedSelfSkills,
    List<Integer> restrictedSummons,
    Set<ClassId> summonBuyerClasses,
    Map<ClassId, List<SkillGrantRule>> grantSkills,
    Map<String, List<Integer>> classSpecificSkills,
    int maxBuffsCount,
    Set<SkillTargetType> targetCheck
) {
    public record SkillGrantRule(int requiredLevel, int skillId, int skillLevel) {}

    
    
    public static BuffShopConfig createDefault() {
                
        return new BuffShopConfig(
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            EnumSet.noneOf(ClassId.class),
            Map.of(),
            Map.of(),
            8,
            BUFFSHOP_TARGET_CHECK
        );
    }
}

public static final Set<SkillTargetType> BUFFSHOP_TARGET_CHECK = EnumSet.of(
        SkillTargetType.SELF,
        SkillTargetType.CORPSE_MOB,
        SkillTargetType.AURA,
        SkillTargetType.AREA,
        SkillTargetType.AREA_CORPSE_MOB,
        SkillTargetType.HOLY
    );
    
}