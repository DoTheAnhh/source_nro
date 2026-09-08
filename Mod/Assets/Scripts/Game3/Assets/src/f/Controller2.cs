using System;
using Game3.Assets.src.g;

namespace Game3.Assets.src.f
{
	internal class Controller2
	{
		public static void readMessage(Message msg)
		{
			try
			{
				switch (msg.command)
				{
				case sbyte.MinValue:
					readInfoEffChar(msg);
					break;
				case sbyte.MaxValue:
					readInfoRada(msg);
					break;
				case 114:
					try
					{
						string text3 = msg.reader().readUTF();
						mSystem.curINAPP = msg.reader().readByte();
						mSystem.maxINAPP = msg.reader().readByte();
						break;
					}
					catch (Exception)
					{
						break;
					}
				case 113:
				{
					int loop = 0;
					int layer = 0;
					int id = 0;
					short x = 0;
					short y = 0;
					short loopCount = -1;
					try
					{
						loop = msg.reader().readByte();
						layer = msg.reader().readByte();
						id = msg.reader().readUnsignedByte();
						x = msg.reader().readShort();
						y = msg.reader().readShort();
						loopCount = msg.reader().readShort();
					}
					catch (Exception)
					{
					}
					EffecMn.addEff(new Effect(id, x, y, layer, loop, loopCount));
					break;
				}
				case 48:
				{
					sbyte b10 = msg.reader().readByte();
					ServerListScreen.ipSelect = b10;
					GameCanvas.instance.doResetToLoginScr(GameCanvas.serverScreen);
					Session_ME.gI().close();
					GameCanvas.endDlg();
					ServerListScreen.waitToLogin = true;
					break;
				}
				case 31:
				{
					int num19 = msg.reader().readInt();
					sbyte b17 = msg.reader().readByte();
					if (b17 == 1)
					{
						short smallID = msg.reader().readShort();
						sbyte b18 = -1;
						int[] array = null;
						short wimg = 0;
						short himg = 0;
						try
						{
							b18 = msg.reader().readByte();
							if (b18 > 0)
							{
								sbyte b19 = msg.reader().readByte();
								array = new int[b19];
								for (int num20 = 0; num20 < b19; num20++)
								{
									array[num20] = msg.reader().readByte();
								}
								wimg = msg.reader().readShort();
								himg = msg.reader().readShort();
							}
						}
						catch (Exception)
						{
						}
						if (num19 == Char.myCharz().charID)
						{
							Char.myCharz().petFollow = new PetFollow();
							Char.myCharz().petFollow.smallID = smallID;
							if (b18 > 0)
							{
								Char.myCharz().petFollow.SetImg(b18, array, wimg, himg);
							}
							break;
						}
						Char char3 = GameScr.findCharInMap(num19);
						char3.petFollow = new PetFollow();
						char3.petFollow.smallID = smallID;
						if (b18 > 0)
						{
							char3.petFollow.SetImg(b18, array, wimg, himg);
						}
					}
					else if (num19 == Char.myCharz().charID)
					{
						Char.myCharz().petFollow.remove();
						Char.myCharz().petFollow = null;
					}
					else
					{
						Char char4 = GameScr.findCharInMap(num19);
						char4.petFollow.remove();
						char4.petFollow = null;
					}
					break;
				}
				case -89:
					GameCanvas.open3Hour = msg.reader().readByte() == 1;
					break;
				case 42:
				{
					GameCanvas.endDlg();
					LoginScr.isContinueToLogin = false;
					Char.isLoadingMap = false;
					sbyte haveName = msg.reader().readByte();
					if (GameCanvas.registerScr == null)
					{
						GameCanvas.registerScr = new RegisterScreen(haveName);
					}
					GameCanvas.registerScr.switchToMe();
					break;
				}
				case 52:
				{
					sbyte b23 = msg.reader().readByte();
					if (b23 == 1)
					{
						int num27 = msg.reader().readInt();
						if (num27 == Char.myCharz().charID)
						{
							Char.myCharz().setMabuHold(true);
							Char.myCharz().cx = msg.reader().readShort();
							Char.myCharz().cy = msg.reader().readShort();
						}
						else
						{
							Char char5 = GameScr.findCharInMap(num27);
							if (char5 != null)
							{
								char5.setMabuHold(true);
								char5.cx = msg.reader().readShort();
								char5.cy = msg.reader().readShort();
							}
						}
					}
					if (b23 == 0)
					{
						int num28 = msg.reader().readInt();
						if (num28 == Char.myCharz().charID)
						{
							Char.myCharz().setMabuHold(false);
						}
						else
						{
							Char char6 = GameScr.findCharInMap(num28);
							if (char6 != null)
							{
								char6.setMabuHold(false);
							}
						}
					}
					if (b23 == 2)
					{
						int charId2 = msg.reader().readInt();
						int id3 = msg.reader().readInt();
						Mabu mabu2 = (Mabu)GameScr.findCharInMap(charId2);
						mabu2.eat(id3);
					}
					if (b23 == 3)
					{
						GameScr.mabuPercent = msg.reader().readByte();
					}
					break;
				}
				case 51:
				{
					int charId = msg.reader().readInt();
					Mabu mabu = (Mabu)GameScr.findCharInMap(charId);
					sbyte id2 = msg.reader().readByte();
					short x2 = msg.reader().readShort();
					short y2 = msg.reader().readShort();
					sbyte b20 = msg.reader().readByte();
					Char[] array2 = new Char[b20];
					int[] array3 = new int[b20];
					for (int num21 = 0; num21 < b20; num21++)
					{
						int num22 = msg.reader().readInt();
						Res.outz("char ID=" + num22);
						array2[num21] = null;
						if (num22 != Char.myCharz().charID)
						{
							array2[num21] = GameScr.findCharInMap(num22);
						}
						else
						{
							array2[num21] = Char.myCharz();
						}
						array3[num21] = msg.reader().readInt();
					}
					mabu.setSkill(id2, x2, y2, array2, array3);
					break;
				}
				case -127:
					readLuckyRound(msg);
					break;
				case -126:
				{
					sbyte b29 = msg.reader().readByte();
					Res.outz("type quay= " + b29);
					if (b29 == 1)
					{
						sbyte b30 = msg.reader().readByte();
						string num42 = msg.reader().readUTF();
						string finish = msg.reader().readUTF();
						GameScr.gI().showWinNumber(num42, finish);
					}
					if (b29 == 0)
					{
						GameScr.gI().showYourNumber(msg.reader().readUTF());
					}
					break;
				}
				case -122:
				{
					short id4 = msg.reader().readShort();
					Npc npc2 = GameScr.findNPCInMap(id4);
					sbyte b28 = msg.reader().readByte();
					npc2.duahau = new int[b28];
					Res.outz("N DUA HAU= " + b28);
					for (int num41 = 0; num41 < b28; num41++)
					{
						npc2.duahau[num41] = msg.reader().readShort();
					}
					npc2.setStatus(msg.reader().readByte(), msg.reader().readInt());
					break;
				}
				case 102:
				{
					sbyte b24 = msg.reader().readByte();
					if (b24 == 0 || b24 == 1 || b24 == 2 || b24 == 6)
					{
						BigBoss2 bigBoss2 = Mob.getBigBoss2();
						if (bigBoss2 == null)
						{
							break;
						}
						if (b24 == 6)
						{
							bigBoss2.x = (bigBoss2.y = (bigBoss2.xTo = (bigBoss2.yTo = (bigBoss2.xFirst = (bigBoss2.yFirst = -1000)))));
							break;
						}
						sbyte b25 = msg.reader().readByte();
						Char[] array7 = new Char[b25];
						int[] array8 = new int[b25];
						for (int num34 = 0; num34 < b25; num34++)
						{
							int num35 = msg.reader().readInt();
							array7[num34] = null;
							if (num35 != Char.myCharz().charID)
							{
								array7[num34] = GameScr.findCharInMap(num35);
							}
							else
							{
								array7[num34] = Char.myCharz();
							}
							array8[num34] = msg.reader().readInt();
						}
						bigBoss2.setAttack(array7, array8, b24);
					}
					if (b24 == 3 || b24 == 4 || b24 == 5 || b24 == 7)
					{
						BachTuoc bachTuoc = Mob.getBachTuoc();
						if (bachTuoc == null)
						{
							break;
						}
						if (b24 == 7)
						{
							bachTuoc.x = (bachTuoc.y = (bachTuoc.xTo = (bachTuoc.yTo = (bachTuoc.xFirst = (bachTuoc.yFirst = -1000)))));
							break;
						}
						if (b24 == 3 || b24 == 4)
						{
							sbyte b26 = msg.reader().readByte();
							Char[] array9 = new Char[b26];
							int[] array10 = new int[b26];
							for (int num36 = 0; num36 < b26; num36++)
							{
								int num37 = msg.reader().readInt();
								array9[num36] = null;
								if (num37 != Char.myCharz().charID)
								{
									array9[num36] = GameScr.findCharInMap(num37);
								}
								else
								{
									array9[num36] = Char.myCharz();
								}
								array10[num36] = msg.reader().readInt();
							}
							bachTuoc.setAttack(array9, array10, b24);
						}
						if (b24 == 5)
						{
							short xMoveTo = msg.reader().readShort();
							bachTuoc.move(xMoveTo);
						}
					}
					if (b24 > 9 && b24 < 30)
					{
						readActionBoss(msg, b24);
					}
					break;
				}
				case 101:
				{
					Res.outz("big boss--------------------------------------------------");
					BigBoss bigBoss = Mob.getBigBoss();
					if (bigBoss == null)
					{
						break;
					}
					sbyte b21 = msg.reader().readByte();
					if (b21 == 0 || b21 == 1 || b21 == 2 || b21 == 4 || b21 == 3)
					{
						if (b21 == 3)
						{
							bigBoss.xTo = (bigBoss.xFirst = msg.reader().readShort());
							bigBoss.yTo = (bigBoss.yFirst = msg.reader().readShort());
							bigBoss.setFly();
						}
						else
						{
							sbyte b22 = msg.reader().readByte();
							Res.outz("CHUONG nChar= " + b22);
							Char[] array4 = new Char[b22];
							int[] array5 = new int[b22];
							for (int num23 = 0; num23 < b22; num23++)
							{
								int num24 = msg.reader().readInt();
								Res.outz("char ID=" + num24);
								array4[num23] = null;
								if (num24 != Char.myCharz().charID)
								{
									array4[num23] = GameScr.findCharInMap(num24);
								}
								else
								{
									array4[num23] = Char.myCharz();
								}
								array5[num23] = msg.reader().readInt();
							}
							bigBoss.setAttack(array4, array5, b21);
						}
					}
					if (b21 == 5)
					{
						bigBoss.haftBody = true;
						bigBoss.status = 2;
					}
					if (b21 == 6)
					{
						bigBoss.getDataB2();
						bigBoss.x = msg.reader().readShort();
						bigBoss.y = msg.reader().readShort();
					}
					if (b21 == 7)
					{
						bigBoss.setAttack(null, null, b21);
					}
					if (b21 == 8)
					{
						bigBoss.xTo = (bigBoss.xFirst = msg.reader().readShort());
						bigBoss.yTo = (bigBoss.yFirst = msg.reader().readShort());
						bigBoss.status = 2;
					}
					if (b21 == 9)
					{
						bigBoss.x = (bigBoss.y = (bigBoss.xTo = (bigBoss.yTo = (bigBoss.xFirst = (bigBoss.yFirst = -1000)))));
					}
					break;
				}
				case -120:
				{
					long num26 = mSystem.currentTimeMillis();
					Service.logController = num26 - Service.curCheckController;
					Service.gI().sendCheckController();
					break;
				}
				case -121:
				{
					long num29 = mSystem.currentTimeMillis();
					Service.logMap = num29 - Service.curCheckMap;
					Service.gI().sendCheckMap();
					break;
				}
				case 100:
				{
					sbyte b31 = msg.reader().readByte();
					sbyte b32 = msg.reader().readByte();
					Item item2 = null;
					if (b31 == 0)
					{
						item2 = Char.myCharz().arrItemBody[b32];
					}
					if (b31 == 1)
					{
						item2 = Char.myCharz().arrItemBag[b32];
					}
					short num43 = msg.reader().readShort();
					if (num43 == -1)
					{
						break;
					}
					item2.template = ItemTemplates.get(num43);
					item2.quantity = msg.reader().readInt();
					item2.info = msg.reader().readUTF();
					item2.content = msg.reader().readUTF();
					sbyte b33 = msg.reader().readByte();
					if (b33 != 0)
					{
						item2.itemOption = new ItemOption[b33];
						for (int num44 = 0; num44 < item2.itemOption.Length; num44++)
						{
							int num45 = msg.reader().readUnsignedByte();
							Res.outz("id o= " + num45);
							int param3 = msg.reader().readUnsignedShort();
							if (num45 != -1)
							{
								item2.itemOption[num44] = new ItemOption(num45, param3);
							}
						}
					}
					if (item2.quantity <= 0)
					{
						item2 = null;
					}
					break;
				}
				case -123:
				{
					int charId3 = msg.reader().readInt();
					if (GameScr.findCharInMap(charId3) != null)
					{
						GameScr.findCharInMap(charId3).perCentMp = msg.reader().readByte();
					}
					break;
				}
				case -119:
					Char.myCharz().rank = msg.reader().readInt();
					break;
				case -117:
					GameScr.gI().tMabuEff = 0;
					GameScr.gI().percentMabu = msg.reader().readByte();
					if (GameScr.gI().percentMabu == 100)
					{
						GameScr.gI().mabuEff = true;
					}
					if (GameScr.gI().percentMabu == 101)
					{
						Npc.mabuEff = true;
					}
					break;
				case -116:
					GameScr.canAutoPlay = msg.reader().readByte() == 1;
					break;
				case -115:
					Char.myCharz().setPowerInfo(msg.reader().readUTF(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort());
					break;
				case -113:
				{
					sbyte[] array6 = new sbyte[10];
					for (int num31 = 0; num31 < 10; num31++)
					{
						array6[num31] = msg.reader().readByte();
						Res.outz("vlue i= " + array6[num31]);
					}
					GameScr.gI().onKSkill(array6);
					GameScr.gI().onOSkill(array6);
					GameScr.gI().onCSkill(array6);
					break;
				}
				case -111:
				{
					short num11 = msg.reader().readShort();
					ImageSource.vSource = new MyVector();
					for (int l = 0; l < num11; l++)
					{
						string iD = msg.reader().readUTF();
						sbyte version = msg.reader().readByte();
						ImageSource.vSource.addElement(new ImageSource(iD, version));
					}
					ImageSource.checkRMS();
					ImageSource.saveRMS();
					break;
				}
				case 125:
				{
					sbyte fusion = msg.reader().readByte();
					int num12 = msg.reader().readInt();
					if (num12 == Char.myCharz().charID)
					{
						Char.myCharz().setFusion(fusion);
					}
					else if (GameScr.findCharInMap(num12) != null)
					{
						GameScr.findCharInMap(num12).setFusion(fusion);
					}
					break;
				}
				case 124:
				{
					short num25 = msg.reader().readShort();
					string text4 = msg.reader().readUTF();
					Res.outz("noi chuyen = " + text4 + "npc ID= " + num25);
					Npc npc = GameScr.findNPCInMap(num25);
					if (npc != null)
					{
						npc.addInfo(text4);
					}
					break;
				}
				case 123:
				{
					Res.outz("SET POSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSss");
					int num4 = msg.reader().readInt();
					short xPos = msg.reader().readShort();
					short yPos = msg.reader().readShort();
					sbyte b4 = msg.reader().readByte();
					Char @char = null;
					if (num4 == Char.myCharz().charID)
					{
						@char = Char.myCharz();
					}
					else if (GameScr.findCharInMap(num4) != null)
					{
						@char = GameScr.findCharInMap(num4);
					}
					if (@char != null)
					{
						ServerEffect.addServerEffect((b4 != 0) ? 173 : 60, @char, 1);
						@char.setPos(xPos, yPos, b4);
					}
					break;
				}
				case 122:
				{
					short num30 = msg.reader().readShort();
					Res.outz("second login = " + num30);
					LoginScr.timeLogin = num30;
					LoginScr.currTimeLogin = (LoginScr.lastTimeLogin = mSystem.currentTimeMillis());
					GameCanvas.endDlg();
					break;
				}
				case 121:
					mSystem.publicID = msg.reader().readUTF();
					mSystem.strAdmob = msg.reader().readUTF();
					Res.outz("SHOW AD public ID= " + mSystem.publicID);
					mSystem.createAdmob();
					break;
				case -124:
				{
					sbyte b7 = msg.reader().readByte();
					sbyte b8 = msg.reader().readByte();
					if (b8 == 0)
					{
						if (b7 == 2)
						{
							int num5 = msg.reader().readInt();
							if (num5 == Char.myCharz().charID)
							{
								Char.myCharz().removeEffect();
							}
							else if (GameScr.findCharInMap(num5) != null)
							{
								GameScr.findCharInMap(num5).removeEffect();
							}
						}
						int num6 = msg.reader().readUnsignedByte();
						int num7 = msg.reader().readInt();
						if (num6 == 32)
						{
							if (b7 == 1)
							{
								int num8 = msg.reader().readInt();
								if (num7 == Char.myCharz().charID)
								{
									Char.myCharz().holdEffID = num6;
									GameScr.findCharInMap(num8).setHoldChar(Char.myCharz());
								}
								else if (GameScr.findCharInMap(num7) != null && num8 != Char.myCharz().charID)
								{
									GameScr.findCharInMap(num7).holdEffID = num6;
									GameScr.findCharInMap(num8).setHoldChar(GameScr.findCharInMap(num7));
								}
								else if (GameScr.findCharInMap(num7) != null && num8 == Char.myCharz().charID)
								{
									GameScr.findCharInMap(num7).holdEffID = num6;
									Char.myCharz().setHoldChar(GameScr.findCharInMap(num7));
								}
							}
							else if (num7 == Char.myCharz().charID)
							{
								Char.myCharz().removeHoleEff();
							}
							else if (GameScr.findCharInMap(num7) != null)
							{
								GameScr.findCharInMap(num7).removeHoleEff();
							}
						}
						if (num6 == 33)
						{
							if (b7 == 1)
							{
								if (num7 == Char.myCharz().charID)
								{
									Char.myCharz().protectEff = true;
								}
								else if (GameScr.findCharInMap(num7) != null)
								{
									GameScr.findCharInMap(num7).protectEff = true;
								}
							}
							else if (num7 == Char.myCharz().charID)
							{
								Char.myCharz().removeProtectEff();
							}
							else if (GameScr.findCharInMap(num7) != null)
							{
								GameScr.findCharInMap(num7).removeProtectEff();
							}
						}
						if (num6 == 39)
						{
							if (b7 == 1)
							{
								if (num7 == Char.myCharz().charID)
								{
									Char.myCharz().huytSao = true;
								}
								else if (GameScr.findCharInMap(num7) != null)
								{
									GameScr.findCharInMap(num7).huytSao = true;
								}
							}
							else if (num7 == Char.myCharz().charID)
							{
								Char.myCharz().removeHuytSao();
							}
							else if (GameScr.findCharInMap(num7) != null)
							{
								GameScr.findCharInMap(num7).removeHuytSao();
							}
						}
						if (num6 == 40)
						{
							if (b7 == 1)
							{
								if (num7 == Char.myCharz().charID)
								{
									Char.myCharz().blindEff = true;
								}
								else if (GameScr.findCharInMap(num7) != null)
								{
									GameScr.findCharInMap(num7).blindEff = true;
								}
							}
							else if (num7 == Char.myCharz().charID)
							{
								Char.myCharz().removeBlindEff();
							}
							else if (GameScr.findCharInMap(num7) != null)
							{
								GameScr.findCharInMap(num7).removeBlindEff();
							}
						}
						if (num6 == 41)
						{
							if (b7 == 1)
							{
								if (num7 == Char.myCharz().charID)
								{
									Char.myCharz().sleepEff = true;
								}
								else if (GameScr.findCharInMap(num7) != null)
								{
									GameScr.findCharInMap(num7).sleepEff = true;
								}
							}
							else if (num7 == Char.myCharz().charID)
							{
								Char.myCharz().removeSleepEff();
							}
							else if (GameScr.findCharInMap(num7) != null)
							{
								GameScr.findCharInMap(num7).removeSleepEff();
							}
						}
						if (num6 == 42)
						{
							if (b7 == 1)
							{
								if (num7 == Char.myCharz().charID)
								{
									Char.myCharz().stone = true;
								}
							}
							else if (num7 == Char.myCharz().charID)
							{
								Char.myCharz().stone = false;
							}
						}
					}
					if (b8 != 1)
					{
						break;
					}
					int num9 = msg.reader().readUnsignedByte();
					sbyte b9 = msg.reader().readByte();
					Res.outz("modbHoldID= " + b9 + " skillID= " + num9 + "eff ID= " + b7);
					if (num9 == 32)
					{
						if (b7 == 1)
						{
							int num10 = msg.reader().readInt();
							if (num10 == Char.myCharz().charID)
							{
								GameScr.findMobInMap(b9).holdEffID = num9;
								Char.myCharz().setHoldMob(GameScr.findMobInMap(b9));
							}
							else if (GameScr.findCharInMap(num10) != null)
							{
								GameScr.findMobInMap(b9).holdEffID = num9;
								GameScr.findCharInMap(num10).setHoldMob(GameScr.findMobInMap(b9));
							}
						}
						else
						{
							GameScr.findMobInMap(b9).removeHoldEff();
						}
					}
					if (num9 == 40)
					{
						if (b7 == 1)
						{
							GameScr.findMobInMap(b9).blindEff = true;
						}
						else
						{
							GameScr.findMobInMap(b9).removeBlindEff();
						}
					}
					if (num9 == 41)
					{
						if (b7 == 1)
						{
							GameScr.findMobInMap(b9).sleepEff = true;
						}
						else
						{
							GameScr.findMobInMap(b9).removeSleepEff();
						}
					}
					break;
				}
				case -125:
				{
					ChatTextField.gI().isShow = false;
					string text = msg.reader().readUTF();
					Res.outz("titile= " + text);
					sbyte b5 = msg.reader().readByte();
					ClientInput.gI().setInput(b5, text);
					for (int k = 0; k < b5; k++)
					{
						ClientInput.gI().tf[k].name = msg.reader().readUTF();
						sbyte b6 = msg.reader().readByte();
						if (b6 == 0)
						{
							ClientInput.gI().tf[k].setIputType(TField.INPUT_TYPE_NUMERIC);
						}
						if (b6 == 1)
						{
							ClientInput.gI().tf[k].setIputType(TField.INPUT_TYPE_ANY);
						}
						if (b6 == 2)
						{
							ClientInput.gI().tf[k].setIputType(TField.INPUT_TYPE_PASSWORD);
						}
					}
					// Bang chi co MOT o thi dua sang HopNhapChu.
					//
					// O chu cua game la o chet tren may tinh: ca than ham
					// TField.keyPressed nam trong khoi chu thich, chi con
					// "return false". Bang nhap cua may chu vi the hien ra
					// nhung khong go duoc chu nao — trong do co bang doi ten
					// cua The doi ten. HopNhapChu tu doc Input.inputString nen
					// go duoc tieng Viet, viet hoa viet thuong va dau cach.
					//
					// Bang nhieu o van di duong cu: gan het la bang cua quan
					// tri, ma HopNhapChu moi lan chi mo duoc mot o.
					if (b5 == 1)
					{
						TField oNhap = ClientInput.gI().tf[0];
						// Ve lai man choi ngay, khong de bang ClientInput dung
						// do phia sau: o chu cua no khong bam vao duoc.
						GameScr.instance.switchToMe();
						// DONG BANG CUA GAME TRUOC KHI MO HOP.
						//
						// Day la cho chua loi "khong nhap duoc code". Form nay hien ra tu
						// MENU NPC — Giftcode o Ong Gohan, doi mat khau, doi ngoc hong —
						// va bang menu do van dang mo phia sau.
						//
						// ClientManager co mot chot an toan: thay bang cua game dang mo
						// (isShow && !isClose) thi no dong HopNhapChu, tru khi hop moi mo
						// chua qua mot giay (co `vuaMo`). Khong dong bang o day thi hop
						// hien ra, song dung MOT GIAY roi bi chot do dong lai — khong ai
						// go xong ma mu 12 ky tu trong mot giay.
						//
						// hide() bat hoat anh truot ra va dat isClose = true, nen chot nhan
						// ra "bang sap bien mat" va bo qua. Day dung la cach may cho khac
						// lam truoc khi mo o nhap: "Mua nhieu", "Dung nhieu", nhap so lan
						// nang cap.
						if (GameCanvas.panel != null && GameCanvas.panel.isShow)
						{
							GameCanvas.panel.hide();
						}
						God.HopNhapChu.getInstance().moRa(oNhap.name,
								string.Empty, 100, delegate(string chu)
								{
									oNhap.setText(chu);
									Service.gI().sendClientInput(
											new TField[] { oNhap });
								});
					}
					break;
				}
				case -110:
				{
					sbyte b27 = msg.reader().readByte();
					if (b27 == 1)
					{
						int num38 = msg.reader().readInt();
						sbyte[] array11 = Rms.loadRMS(num38 + string.Empty);
						if (array11 == null)
						{
							Service.gI().sendServerData(1, -1, null);
						}
						else
						{
							Service.gI().sendServerData(1, num38, array11);
						}
					}
					if (b27 == 0)
					{
						int num39 = msg.reader().readInt();
						short num40 = msg.reader().readShort();
						sbyte[] data = new sbyte[num40];
						msg.reader().read(ref data, 0, num40);
						Rms.saveRMS(num39 + string.Empty, data);
					}
					break;
				}
				// Chat ban do (60) va chat khu (67).
				//
				// Cung dang goi voi chat the gioi: ten nguoi gui roi toi noi
				// dung. Doc xong day thang vao khung chat, khong ve gi them.
				case 60:
				{
					string tenM = msg.reader().readUTF();
					string chuM = msg.reader().readUTF();
					try
					{
						God.ChatUI.getInstance().nhanChatMap(tenM, chuM);
					}
					catch (Exception)
					{
					}
					break;
				}
				case 67:
				{
					string tenK = msg.reader().readUTF();
					string chuK = msg.reader().readUTF();
					try
					{
						God.ChatUI.getInstance().nhanChatKhu(tenK, chuK);
					}
					catch (Exception)
					{
					}
					break;
				}
				case 93:
				{
					string str = msg.reader().readUTF();
					str = Res.changeString(str);
					// Cau loa vao thang the "He thong" cua khung chat.
					//
					// Truoc day no CHI chay o dai chu duoi day man hinh; dai do
					// da bo, khong dua vao day thi cau loa mat han.
					try
					{
						God.ChatUI.getInstance().nhanTinMayChu(str);
					}
					catch (Exception)
					{
					}
					// Cau loa bao boss xuat hien di chung duong voi moi cau loa
					// khac. Tach ra tai day de khung thong bao boss goc tren ben
					// trai co du lieu — truoc day getBOSSInfo khong noi nao goi
					// nen danh sach luon rong, khung khong ve gi ca.
					if (str != null && str.Contains(" vừa xuất hiện tại "))
					{
						try
						{
							Game3.God.BossData.getInstance().getBOSSInfo(str);
						}
						catch (Exception)
						{
						}
					}
					break;
				}
				case -106:
				{
					short num32 = msg.reader().readShort();
					int num33 = msg.reader().readShort();
					if (ItemTime.isExistItem(num32))
					{
						ItemTime.getItemById(num32).initTime(num33);
						break;
					}
					ItemTime o = new ItemTime(num32, num33);
					Char.vItemTime.addElement(o);
					break;
				}
				case -105:
					TransportScr.gI().time = 0;
					TransportScr.gI().maxTime = msg.reader().readShort();
					TransportScr.gI().last = (TransportScr.gI().curr = mSystem.currentTimeMillis());
					TransportScr.gI().type = msg.reader().readByte();
					TransportScr.gI().switchToMe();
					break;
				case -103:
				{
					sbyte b12 = msg.reader().readByte();
					if (b12 == 0)
					{
						GameCanvas.panel.vFlag.removeAllElements();
						sbyte b13 = msg.reader().readByte();
						for (int m = 0; m < b13; m++)
						{
							Item item = new Item();
							short num13 = msg.reader().readShort();
							if (num13 != -1)
							{
								item.template = ItemTemplates.get(num13);
								sbyte b14 = msg.reader().readByte();
								if (b14 != -1)
								{
									item.itemOption = new ItemOption[b14];
									for (int n = 0; n < item.itemOption.Length; n++)
									{
										int num14 = msg.reader().readUnsignedByte();
										int param2 = msg.reader().readUnsignedShort();
										if (num14 != -1)
										{
											item.itemOption[n] = new ItemOption(num14, param2);
										}
									}
								}
							}
							GameCanvas.panel.vFlag.addElement(item);
						}
						// Mo bang chon co moi thay cho Panel loai TYPE_FLAG.
						//
						// Panel la mot doi tuong dung chung cho hon hai muoi loai
						// noi dung; bam ra ngoai no khong dong ma quay ve loai
						// noi dung TRUOC DO, nen thoat bang co lai hien ra menu
						// "Chuc Nang".
						//
						// Van do du lieu vao `panel.vFlag` o tren: do la cho
						// Panel dung san, giu nguyen thi khong dong gi cua Panel.
						God.CoUI.getInstance().nhanDuLieu(GameCanvas.panel.vFlag);
					}
					else if (b12 == 1)
					{
						int num15 = msg.reader().readInt();
						sbyte b15 = msg.reader().readByte();
						Res.outz("---------------actionFlag1:  " + num15 + " : " + b15);
						if (num15 == Char.myCharz().charID)
						{
							Char.myCharz().cFlag = b15;
						}
						else if (GameScr.findCharInMap(num15) != null)
						{
							GameScr.findCharInMap(num15).cFlag = b15;
						}
						GameScr.gI().getFlagImage(num15, b15);
					}
					else
					{
						if (b12 != 2)
						{
							break;
						}
						sbyte b16 = msg.reader().readByte();
						int num16 = msg.reader().readShort();
						PKFlag pKFlag = new PKFlag();
						pKFlag.cflag = b16;
						pKFlag.IDimageFlag = num16;
						GameScr.vFlag.addElement(pKFlag);
						for (int num17 = 0; num17 < GameScr.vFlag.size(); num17++)
						{
							PKFlag pKFlag2 = (PKFlag)GameScr.vFlag.elementAt(num17);
							Res.outz("i: " + num17 + "  cflag: " + pKFlag2.cflag + "   IDimageFlag: " + pKFlag2.IDimageFlag);
						}
						for (int num18 = 0; num18 < GameScr.vCharInMap.size(); num18++)
						{
							Char char2 = (Char)GameScr.vCharInMap.elementAt(num18);
							if (char2 != null && char2.cFlag == b16)
							{
								char2.flagImage = num16;
							}
						}
						if (Char.myCharz().cFlag == b16)
						{
							Char.myCharz().flagImage = num16;
						}
					}
					break;
				}
				case -102:
				{
					sbyte b11 = msg.reader().readByte();
					if (b11 != 0 && b11 == 1)
					{
						GameCanvas.loginScr.isLogin2 = false;
						Service.gI().login(Rms.loadRMSString("acc3"), Rms.loadRMSString("pass3"), GameMidlet.VERSION, 0);
						LoginScr.isLoggingIn = true;
					}
					break;
				}
				case -101:
				{
					GameCanvas.loginScr.isLogin2 = true;
					GameCanvas.connect();
					string text2 = msg.reader().readUTF();
					Rms.saveRMSString("userAo3" + ServerListScreen.ipSelect, text2);
					Service.gI().setClientType();
					Service.gI().login(text2, string.Empty, GameMidlet.VERSION, 1);
					break;
				}
				case -100:
				{
					InfoDlg.hide();
					bool flag = false;
					if (GameCanvas.w > 2 * Panel.WIDTH_PANEL)
					{
						flag = true;
					}
					sbyte b = msg.reader().readByte();
					if (b < 0)
					{
						break;
					}
					Res.outz("t Indxe= " + b);
					GameCanvas.panel.maxPageShop[b] = msg.reader().readByte();
					GameCanvas.panel.currPageShop[b] = msg.reader().readByte();
					Res.outz("max page= " + GameCanvas.panel.maxPageShop[b] + " curr page= " + GameCanvas.panel.currPageShop[b]);
					int num = msg.reader().readUnsignedByte();
					Char.myCharz().arrItemShop[b] = new Item[num];
					for (int i = 0; i < num; i++)
					{
						short num2 = msg.reader().readShort();
						if (num2 == -1)
						{
							continue;
						}
						Res.outz("template id= " + num2);
						Char.myCharz().arrItemShop[b][i] = new Item();
						Char.myCharz().arrItemShop[b][i].template = ItemTemplates.get(num2);
						Char.myCharz().arrItemShop[b][i].itemId = msg.reader().readShort();
						Char.myCharz().arrItemShop[b][i].buyCoin = msg.reader().readInt();
						Char.myCharz().arrItemShop[b][i].buyGold = msg.reader().readInt();
						Char.myCharz().arrItemShop[b][i].buyType = msg.reader().readByte();
						Char.myCharz().arrItemShop[b][i].quantity = msg.reader().readInt();
						Char.myCharz().arrItemShop[b][i].isMe = msg.reader().readByte();
						Panel.strWantToBuy = mResources.say_wat_do_u_want_to_buy;
						sbyte b2 = msg.reader().readByte();
						if (b2 != -1)
						{
							Char.myCharz().arrItemShop[b][i].itemOption = new ItemOption[b2];
							for (int j = 0; j < Char.myCharz().arrItemShop[b][i].itemOption.Length; j++)
							{
								int num3 = msg.reader().readUnsignedByte();
								int param = msg.reader().readUnsignedShort();
								if (num3 != -1)
								{
									Char.myCharz().arrItemShop[b][i].itemOption[j] = new ItemOption(num3, param);
									Char.myCharz().arrItemShop[b][i].compare = GameCanvas.panel.getCompare(Char.myCharz().arrItemShop[b][i]);
								}
							}
						}
						sbyte b3 = msg.reader().readByte();
						if (b3 == 1)
						{
							int headTemp = msg.reader().readShort();
							int bodyTemp = msg.reader().readShort();
							int legTemp = msg.reader().readShort();
							int bagTemp = msg.reader().readShort();
							Char.myCharz().arrItemShop[b][i].setPartTemp(headTemp, bodyTemp, legTemp, bagTemp);
						}
						if (GameMidlet.intVERSION >= 237)
						{
							Char.myCharz().arrItemShop[b][i].nameNguoiKyGui = msg.reader().readUTF();
							Res.err("nguoi ki gui  " + Char.myCharz().arrItemShop[b][i].nameNguoiKyGui);
						}
					}
					if (flag)
					{
						GameCanvas.panel2.setTabKiGui();
					}
					GameCanvas.panel.setTabShop();
					GameCanvas.panel.cmy = (GameCanvas.panel.cmtoY = 0);
					break;
				}
				}
			}
			catch (Exception ex4)
			{
				Res.outz("=====> Controller2 " + ex4.StackTrace);
			}
		}

		private static void readLuckyRound(Message msg)
		{
			try
			{
				sbyte b = msg.reader().readByte();
				if (b == 0)
				{
					sbyte b2 = msg.reader().readByte();
					short[] array = new short[b2];
					for (int i = 0; i < b2; i++)
					{
						array[i] = msg.reader().readShort();
					}
					sbyte b3 = msg.reader().readByte();
					int price = msg.reader().readInt();
					short idTicket = msg.reader().readShort();
					CrackBallScr.gI().SetCrackBallScr(array, (byte)b3, price, idTicket);
				}
				else if (b == 1)
				{
					sbyte b4 = msg.reader().readByte();
					short[] array2 = new short[b4];
					for (int j = 0; j < b4; j++)
					{
						array2[j] = msg.reader().readShort();
					}
					CrackBallScr.gI().DoneCrackBallScr(array2);
				}
			}
			catch (Exception)
			{
			}
		}

		private static void readInfoRada(Message msg)
		{
			try
			{
				sbyte b = msg.reader().readByte();
				if (b == 0)
				{
					RadarScr.gI();
					MyVector myVector = new MyVector(string.Empty);
					short num = msg.reader().readShort();
					int num2 = 0;
					for (int i = 0; i < num; i++)
					{
						Info_RadaScr info_RadaScr = new Info_RadaScr();
						int id = msg.reader().readShort();
						int no = i + 1;
						int idIcon = msg.reader().readShort();
						sbyte rank = msg.reader().readByte();
						sbyte amount = msg.reader().readByte();
						sbyte max_amount = msg.reader().readByte();
						short templateId = -1;
						Char charInfo = null;
						sbyte b2 = msg.reader().readByte();
						if (b2 == 0)
						{
							templateId = msg.reader().readShort();
						}
						else
						{
							int head = msg.reader().readShort();
							int body = msg.reader().readShort();
							int leg = msg.reader().readShort();
							int bag = msg.reader().readShort();
							charInfo = Info_RadaScr.SetCharInfo(head, body, leg, bag);
						}
						string name = msg.reader().readUTF();
						string info = msg.reader().readUTF();
						sbyte b3 = msg.reader().readByte();
						sbyte use = msg.reader().readByte();
						sbyte b4 = msg.reader().readByte();
						ItemOption[] array = null;
						if (b4 != 0)
						{
							array = new ItemOption[b4];
							for (int j = 0; j < array.Length; j++)
							{
								int num3 = msg.reader().readUnsignedByte();
								int param = msg.reader().readUnsignedShort();
								sbyte activeCard = msg.reader().readByte();
								if (num3 != -1)
								{
									array[j] = new ItemOption(num3, param);
									array[j].activeCard = activeCard;
								}
							}
						}
						info_RadaScr.SetInfo(id, no, idIcon, rank, b2, templateId, name, info, charInfo, array);
						info_RadaScr.SetLevel(b3);
						info_RadaScr.SetUse(use);
						info_RadaScr.SetAmount(amount, max_amount);
						myVector.addElement(info_RadaScr);
						if (b3 > 0)
						{
							num2++;
						}
					}
					RadarScr.gI().SetRadarScr(myVector, num2, num);
					RadarScr.gI().switchToMe();
				}
				else if (b == 1)
				{
					int id2 = msg.reader().readShort();
					sbyte use2 = msg.reader().readByte();
					if (Info_RadaScr.GetInfo(RadarScr.list, id2) != null)
					{
						Info_RadaScr.GetInfo(RadarScr.list, id2).SetUse(use2);
					}
					RadarScr.SetListUse();
				}
				else if (b == 2)
				{
					int num4 = msg.reader().readShort();
					sbyte level = msg.reader().readByte();
					int num5 = 0;
					for (int k = 0; k < RadarScr.list.size(); k++)
					{
						Info_RadaScr info_RadaScr2 = (Info_RadaScr)RadarScr.list.elementAt(k);
						if (info_RadaScr2 != null)
						{
							if (info_RadaScr2.id == num4)
							{
								info_RadaScr2.SetLevel(level);
							}
							if (info_RadaScr2.level > 0)
							{
								num5++;
							}
						}
					}
					RadarScr.SetNum(num5, RadarScr.list.size());
					if (Info_RadaScr.GetInfo(RadarScr.listUse, num4) != null)
					{
						Info_RadaScr.GetInfo(RadarScr.listUse, num4).SetLevel(level);
					}
				}
				else if (b == 3)
				{
					int id3 = msg.reader().readShort();
					sbyte amount2 = msg.reader().readByte();
					sbyte max_amount2 = msg.reader().readByte();
					if (Info_RadaScr.GetInfo(RadarScr.list, id3) != null)
					{
						Info_RadaScr.GetInfo(RadarScr.list, id3).SetAmount(amount2, max_amount2);
					}
					if (Info_RadaScr.GetInfo(RadarScr.listUse, id3) != null)
					{
						Info_RadaScr.GetInfo(RadarScr.listUse, id3).SetAmount(amount2, max_amount2);
					}
				}
				else if (b == 4)
				{
					int num6 = msg.reader().readInt();
					short idAuraEff = msg.reader().readShort();
					Char @char = null;
					@char = ((num6 != Char.myCharz().charID) ? GameScr.findCharInMap(num6) : Char.myCharz());
					if (@char != null)
					{
						@char.idAuraEff = idAuraEff;
						@char.idEff_Set_Item = msg.reader().readByte();
					}
				}
			}
			catch (Exception)
			{
			}
		}

		private static void readInfoEffChar(Message msg)
		{
			try
			{
				sbyte b = msg.reader().readByte();
				int num = msg.reader().readInt();
				Char @char = null;
				@char = ((num != Char.myCharz().charID) ? GameScr.findCharInMap(num) : Char.myCharz());
				if (b == 0)
				{
					int id = msg.reader().readShort();
					int layer = msg.reader().readByte();
					int loop = msg.reader().readByte();
					short loopCount = msg.reader().readShort();
					sbyte isStand = msg.reader().readByte();
					if (@char != null)
					{
						@char.addEffChar(new Effect(id, @char, layer, loop, loopCount, isStand));
					}
				}
				else if (b == 1)
				{
					int id2 = msg.reader().readShort();
					if (@char != null)
					{
						@char.removeEffChar(0, id2);
					}
				}
				else if (b == 2 && @char != null)
				{
					@char.removeEffChar(-1, 0);
				}
			}
			catch (Exception)
			{
			}
		}

		private static void readActionBoss(Message msg, int actionBoss)
		{
			try
			{
				sbyte idBoss = msg.reader().readByte();
				NewBoss newBoss = Mob.getNewBoss(idBoss);
				if (newBoss == null)
				{
					return;
				}
				if (actionBoss == 10)
				{
					short xMoveTo = msg.reader().readShort();
					short yMoveTo = msg.reader().readShort();
					newBoss.move(xMoveTo, yMoveTo);
				}
				if (actionBoss >= 11 && actionBoss <= 20)
				{
					sbyte b = msg.reader().readByte();
					Char[] array = new Char[b];
					int[] array2 = new int[b];
					for (int i = 0; i < b; i++)
					{
						int num = msg.reader().readInt();
						array[i] = null;
						if (num != Char.myCharz().charID)
						{
							array[i] = GameScr.findCharInMap(num);
						}
						else
						{
							array[i] = Char.myCharz();
						}
						array2[i] = msg.reader().readInt();
					}
					sbyte dir = msg.reader().readByte();
					newBoss.setAttack(array, array2, (sbyte)(actionBoss - 10), dir);
				}
				if (actionBoss == 21)
				{
					newBoss.xTo = msg.reader().readShort();
					newBoss.yTo = msg.reader().readShort();
					newBoss.setFly();
				}
				if (actionBoss == 22)
				{
				}
				if (actionBoss == 23)
				{
					newBoss.setDie();
				}
			}
			catch (Exception)
			{
			}
		}
	}
}
