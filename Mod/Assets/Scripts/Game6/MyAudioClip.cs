using UnityEngine;

namespace Game6
{
    
    public class MyAudioClip
    {
    	public string name;
    
    	public AudioClip clip;
    
    	public long timeStart;
    
    	public MyAudioClip(string filename)
    	{
    		clip = (AudioClip)Resources.Load(filename);
    		name = filename;
    	}
    
    	public void Play()
    	{
    		Main.main.GetComponent<AudioSource>().PlayOneShot(clip);
    		timeStart = mSystem.currentTimeMillis();
    	}
    
    	public bool isPlaying()
    	{
    		return false;
    	}
    }
}
