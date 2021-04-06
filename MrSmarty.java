package rs.ac.bg.etf.players;

public class MrSmarty extends Player{
	private static final Move first=Move.PUT1COIN;
	private static int entered;
	private static int maxError=2;
	private static int roundCountMemorizer=-1;
	
	private int errorCounter;
	private int discoverySteps;
	private boolean suspicious;
	private PlayerStrategy strategy; 
	private int roundCounter;
	private Move prev;
	/*
	 * strategije sa kojima igram protiv igraca
	 * G-Goody,S-Stinger,F-Forgiver,C-Copycat,A-Avenger,My-MrSmarty
	 * 
	 */
	private enum PlayerStrategy{
		G,S,F,C,A,My;
		static boolean b=true;
		static void resetB() {
			b=true;
		}
		Move getMove() { 
			switch(this) {
			case G: case S: return Move.DONTPUTCOINS;
			case A: return Move.PUT1COIN;
			case F: b=!b; return (b)?Move.PUT2COINS:Move.DONTPUTCOINS;
			default: return Move.PUT2COINS;
			}
		}
		Move getExpected() {
			switch(this) {
			case S: return Move.DONTPUTCOINS;
			case A: return Move.PUT1COIN;
			default: return Move.PUT2COINS;
			}
		}
	}
	
	/*
	 * poziva se na kraju svake igre da resetuje stanje 
	 */
	
	@Override
	public void resetPlayerState() {
		// TODO Auto-generated method stub
		super.resetPlayerState();
		entered=0;
		errorCounter=0;
		discoverySteps=0;
		strategy=null;
		suspicious=false;
		if(roundCountMemorizer==-1) {
			roundCountMemorizer=roundCounter;
			maxError=roundCountMemorizer/10+1; 
		}
		roundCounter=0;
		prev=null;
		PlayerStrategy.resetB();
	}
	
	
	private Move handleError() {
		
		int lastElement=opponentMoves.size()-1;
		Move lastOpponentMove=opponentMoves.get(lastElement);
		
		if(strategy!=null&&strategy.equals(PlayerStrategy.My)) { //gresku naseg igraca ignorisemo
			return strategy.getMove(); 
		}
		
		if(++errorCounter>=maxError) {
			/*
			 * ako smo prekoracili neku granicu greske (da je greska preko 10%), ponasacemo se kao Stinger
			 * jer sa velikom greskom, koja se priblizava random strategiji, najbezbednije je igrati dominantnu strategiju
			 * pritom ako je doslo do greske koja nas je nagradila (dala nam vise poena od ocekivanog), nema potrebe kaznjavati
			 * 
			 */
			if(prev.ordinal()>lastOpponentMove.ordinal()) strategy=PlayerStrategy.S; 
			return strategy.getMove();
		}
		if(suspicious==false&&strategy!=null) {
			/*
			 * ako se desila jedna greska, ne menjamo strategiju, vec samo oznacavamo da je protivnik sumnjiv
			 * kada je procenat greske mali to je znacajno (posebno ako igramo sa Avengerom)
			 * 
			 */
			suspicious=true;
			return strategy.getMove();
			
		}
		/*
		 * ako je doslo do greske pre nego sto smo otkrili strategiju protivnika, ili ako je dva puta za redom doslo do greske 
		 * nastavljamo da igramo kao sa Stingerom, odnosno sve 0, nema potrebe dalje gubiti poene
		 * 
		 */
		strategy=PlayerStrategy.S;
		return strategy.getMove();
		
	}
	
	/*
	 * otkriva protivnika
	 * 
	 */
	private Move discoverOpponent(Move last) {
		discoverySteps++;
		
		if(entered==2) {
			strategy=PlayerStrategy.My;
			return strategy.getMove();
		}
		
		if(discoverySteps==1) {//prva runda
			switch(last) {
				case DONTPUTCOINS: return Move.DONTPUTCOINS;
				case PUT1COIN: return Move.PUT2COINS; 
				case PUT2COINS: return Move.PUT1COIN;
			}	
		}
		
		
		if(discoverySteps==2) {//druga runda - otkriva sve osim FC
			Move firstO=opponentMoves.get(0);
			if(firstO.equals(Move.DONTPUTCOINS)) {
				if(last.equals(Move.DONTPUTCOINS)) {
					strategy=PlayerStrategy.S;
				}
				else return handleError();
			}
			
			if(firstO.equals(Move.PUT2COINS)) {
				switch(last) {
				case DONTPUTCOINS: return handleError();
				case PUT1COIN: strategy=PlayerStrategy.A; break;
				case PUT2COINS: strategy=PlayerStrategy.G; break;
				}
			}
			
			if(firstO.equals(Move.PUT1COIN)) {
				switch(last) {
				case DONTPUTCOINS: return handleError();
				case PUT1COIN: return Move.DONTPUTCOINS;
				case PUT2COINS: strategy=PlayerStrategy.My; break;
				}
			}
			return strategy.getMove();
		}
		
		if(discoverySteps==3) {//u tecoj rundi ulazi samo ako je FC ili greska
			switch(last) {
			case PUT2COINS: return Move.PUT2COINS;
			default: return handleError();
			}
			
		}
		
		if(discoverySteps==4) { //cetvrta runda 
			switch(last) {
			case DONTPUTCOINS: strategy=PlayerStrategy.C; break;
			case PUT1COIN: return handleError();
			case PUT2COINS: strategy=PlayerStrategy.F; break;
			}
			return strategy.getMove();
		}
		return null;
	}
	
	
	@Override
	public Move getNextMove() {
		// TODO Auto-generated method stub
		
		roundCounter++;
		if(roundCounter==1) { //prva runda, samo ulazi u mec
			entered++;
			return first;
		}
		
		int lastElement=opponentMoves.size()-1;
		Move lastOpponentMove=opponentMoves.get(lastElement);
		Move next=null;
		
		if(strategy==null) next=discoverOpponent(lastOpponentMove); //ako nije otkrivena strategija jos je otkriva
		else {
			
			//proveri da li je mozda protivnik pogresio
			if(strategy.getExpected().equals(lastOpponentMove)) {
				
				if(roundCounter==roundCountMemorizer&&!strategy.equals(PlayerStrategy.My)) next=PlayerStrategy.S.getMove(); //poslednji potez baca 0
				else next= strategy.getMove();
				suspicious=false;
				
			}
			else {
				//System.out.println("gresku baca jer ocekujem strategiju"+strategy.getExpected()+"a dobija"+lastOpponentMove+" strategija je "+strategy+"rund je "+roundCounter);
				next=handleError(); 
			}
		}
		prev=next;
		return next;
	}

}
