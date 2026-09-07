package nro.entity.skill;

import java.util.ArrayList;
import java.util.List;
import nro.core.util.Util;
import nro.entity.template.SkillTemplate;


public class NClass {

    public int classId;

    public String name;
    
    public List<SkillTemplate> skillTemplatess = new ArrayList<>();
    
    public SkillTemplate getSkillTemplate(int tempId){
        for (SkillTemplate skillTemplate : skillTemplatess) {
            if (skillTemplate.id == tempId){
                return skillTemplate;
            }
        }
        return null;
    }
    
    public SkillTemplate getSkillTemplateByName(String name){
        for (SkillTemplate skillTemplate : skillTemplatess) {
            if((Util.removeAccent(skillTemplate.name).toUpperCase()).contains((Util.removeAccent(name)).toUpperCase())){
                return skillTemplate;
           }
        }
        return null;
    }
    
}





