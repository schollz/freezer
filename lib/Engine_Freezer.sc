// Engine_Freezer

// Inherit methods from CroneEngine
Engine_Freezer : CroneEngine {

	// Freezer specific v0.1.0
	var server;
	var params;
	var syns;
	var oscs;
	// Freezer ^


	*new { arg context, doneCallback;
		^super.new(context, doneCallback);
	}

	alloc {
		// Freezer specific v0.0.1
		var server = context.server;

        SynthDef("freezer",{
            arg bufL, bufR, freeze=0, freezeTime=2.0, freezeFadePos=0, rate=1, impulseFreq = 10, gate = 1;
            var sndIn, sndGrain, snd, impulse;
            var freezeFader;
            var freezeRecord = Latch.kr(freeze,TDelay.kr(Changed.kr(freeze),freezeTime));
            impulse = Impulse.kr(LFNoise2.kr(1/3).range(0.9,1.1)*impulseFreq);
            sndIn = SoundIn.ar([0,1]);
            RecordBuf.ar(
                inputArray: sndIn[0],
                bufnum: bufL,
                recLevel: VarLag.kr(1-freezeRecord,warp:\sine),
                preLevel: VarLag.kr(freezeRecord,warp:\sine),
            );
            RecordBuf.ar(
                inputArray: sndIn[1],
                bufnum: bufR,
                recLevel: VarLag.kr(1-freezeRecord,warp:\sine),
                preLevel: VarLag.kr(freezeRecord,warp:\sine),
            );
            sndGrain = GrainBuf.ar(
                numChannels: 2,
                trigger: impulse + Dust.kr(impulseFreq/10),
                dur: LFNoise2.kr(1).range(0.6,1.5)*1/impulseFreq,
                sndbuf: bufL,
                rate: rate,
                pos: LFNoise0.kr(10).range(0.2,0.8),
                pan: -1,
                maxGrains: 256,
            ) + GrainBuf.ar(
                numChannels: 2,
                trigger: impulse + Dust.kr(impulseFreq/10),
                dur: LFNoise2.kr(1).range(0.8,1.2)*1/impulseFreq,
                sndbuf: bufR,
                rate: rate,
                pos: LFNoise0.kr(10).range(0.2,0.8),
                pan: 1,
                maxGrains: 256,
            );
            freezeFader = VarLag.kr(freezeFadePos,0.2,warp:\sine);
			SendReply.kr(Impulse.kr(10*gate),"/state",[freezeFader]);
            snd = SelectX.ar(freezeFader,[sndIn,sndGrain+(LFNoise2.kr(1/3).range(0.2,0.5)*[
                PlayBuf.ar(1,bufL,loop:1),
                PlayBuf.ar(1,bufR,loop:1),
            ])]);
            snd = snd * EnvGen.ar(Env.adsr(3,1,1,3),gate:gate,doneAction:2);
            Out.ar(0,snd);
        }).add;


		// initialize variables
		params = Dictionary.new();
		syns = Dictionary.new();
    oscs = Dictionary.new();

    oscs.put("state",OSCFunc({ |msg|
      NetAddr("127.0.0.1", 10111).sendMsg("freezeFader",msg[3]);
		}, '/state'));

		this.addCommand("freezer","f",{ arg msg;
			var seconds=msg[1].asFloat;
            var args=[];
            params.keysValuesDo({ arg k, v;
                args=args++[k,v];
            });
    
            if (syns.at("freezer").notNil,{
                syns.at("freezer").set(\gate,0);
            });
            Buffer.alloc(server,2*server.sampleRate,1, completionMessage:{ arg bufL;
                Buffer.alloc(server,2*server.sampleRate,1, completionMessage:{ arg bufR;
                    args=args++[\bufL,bufL,\bufR,bufR];
                    syns.put("freezer",Synth.head(server,"freezer",args).onFree({
                        bufL.free;
                        bufR.free;
                    }));
                    NodeWatcher.register(syns.at("freezer"));
                });
            });
		});

		this.addCommand("set","sf",{ arg msg;
			var k=msg[1].asSymbol;
			var v=msg[2];
            params.put(k,v);
            if (syns.at("freezer").notNil,{
                syns.at("freezer").set(k,v);
            });
		});

		
	}


	free {
		syns.keysValuesDo({ arg k, val;
			val.free;
		});
		oscs.keysValuesDo({ arg k, val;
			val.free;
		});
	}
}
