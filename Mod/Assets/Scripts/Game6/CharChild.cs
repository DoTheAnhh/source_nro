
namespace Game6
{
    public class CharChild : Char
    {
        public Skill skill;
        public CharChild(int head, int body, int leg, int cgender, int cx, int cy, int cdir) : base()
        {
            base.cgender = cgender;
            base.cx = cx;
            base.cy = cy;
            base.cdir = cdir;
            base.head = head;
            base.body = body;
            base.leg = leg;
            skill = Skills.get(cgender switch
            {
                0 => 24,
                1 => 26,
                2 => 25,
                _ => -1
            });
        }
        public override void update()
        {
            EffectManager.update();
            base.update();
        }
        public override void setSkillPaint(SkillPaint skillPaint, int sType)
        {
            Res.outz("skill id= " + skillPaint.id);
            if (isMonkey == 1 && skillPaint.id >= 35 && skillPaint.id <= 41)
            {
                skillPaint = GameScr.sks[106];
            }
            if (skillPaint.id >= 128 && skillPaint.id <= 134)
            {
                skillPaint = GameScr.sks[skillPaint.id - 65];
                ServerEffect.addServerEffect(60, cx, cy, 1);
                telePortSkill = true;
            }
            if (skillPaint.id >= 107 && skillPaint.id <= 113)
            {
                skillPaint = GameScr.sks[skillPaint.id - 44];
                EffecMn.addEff(new Effect(23, cx, cy + ch / 2, 3, 2, 1));
            }
            setAutoSkillPaint(skillPaint, sType);
        }
        public override void paintCharBody(mGraphics g, int cx, int cy, int cdir, int cf, bool isPaintBag)
        {
            var ph = GameScr.parts[head];
            var pl = GameScr.parts[leg];
            var pb = GameScr.parts[body];
            int num = 2;
            int anchor = 24;
            int num2 = -1;
            if (cdir == 1)
            {
                num = 0;
                anchor = 0;
                num2 = 1;
            }
        //    paintHat_behind(g, cf, cy - CharInfo[cf][2][2] + pb.pi[CharInfo[cf][2][0]].dy);
            SmallImage.drawSmallImage(g, ph.pi[CharInfo[cf][0][0]].id, cx + (CharInfo[cf][0][1] + ph.pi[CharInfo[cf][0][0]].dx) * num2, cy - CharInfo[cf][0][2] + ph.pi[CharInfo[cf][0][0]].dy, num, 0);
            SmallImage.drawSmallImage(g, pl.pi[CharInfo[cf][1][0]].id, cx + (CharInfo[cf][1][1] + pl.pi[CharInfo[cf][1][0]].dx) * num2, cy - CharInfo[cf][1][2] + pl.pi[CharInfo[cf][1][0]].dy, num, 0);
            SmallImage.drawSmallImage(g, pb.pi[CharInfo[cf][2][0]].id, cx + (CharInfo[cf][2][1] + pb.pi[CharInfo[cf][2][0]].dx) * num2, cy - CharInfo[cf][2][2] + pb.pi[CharInfo[cf][2][0]].dy, num, 0);
            ch = ((isMonkey != 1 && !isFusion) ? (CharInfo[0][0][2] + ph.pi[CharInfo[0][0][0]].dy + 10) : 60);
            int num4 = ((Res.abs(ph.pi[CharInfo[cf][0][0]].dy) < 22) ? ph.pi[CharInfo[cf][0][0]].dy : ((ph.pi[CharInfo[cf][0][0]].dy >= 0) ? (ph.pi[CharInfo[cf][0][0]].dy - 5) : (ph.pi[CharInfo[cf][0][0]].dy + 5)));
            cH_new = cy - CharInfo[cf][0][2] + num4;
        }
    }
}
