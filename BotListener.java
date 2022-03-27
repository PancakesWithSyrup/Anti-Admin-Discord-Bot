import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogChange;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Map;

class BotListener extends ListenerAdapter {
    
    @Override
    public void onGuildVoiceGuildMute(GuildVoiceGuildMuteEvent event) {
        Guild guild = event.getGuild();
        guild.retrieveAuditLogs().type(ActionType.MEMBER_UPDATE).limit(1).queue(entryLog -> {
            if(entryLog.isEmpty())
                return;
            AuditLogEntry entry = entryLog.get(0);
            // Make sure the user muted another user before continuing as onGuildVoiceMute will also trigger
            // whenever a user unmutes themselves or another user.
            Map<String, AuditLogChange> entryChanges = entry.getChanges();
            if(entryChanges.get("mute").getNewValue().equals(false))
                return;
            Member sourceMember = guild.getMember(entry.getUser());
            // If the bot did the muting then ignore.
            if(sourceMember.getUser().isBot())
                return;
            Member targetMember = event.getMember();
            guild.mute(targetMember, false).queue();
            guild.mute(sourceMember, true).queue();
        });
    }
    @Override
    public void onGuildVoiceGuildDeafen(GuildVoiceGuildDeafenEvent event) {
        Guild guild = event.getGuild();
        guild.retrieveAuditLogs().type(ActionType.MEMBER_UPDATE).limit(1).queue(entryLog -> {
            if(entryLog.isEmpty())
                return;
            AuditLogEntry entry = entryLog.get(0);
            // Make sure the user muted another user before continuing as onGuildVoiceGuildDeafen will also trigger
            // whenever a user undeafens themselves or another user.
            Map<String, AuditLogChange> entryChanges = entry.getChanges();
            if(entryChanges.get("deafen").getNewValue().equals(false))
                return;
            Member sourceMember = guild.getMember(entry.getUser());
            // If the bot did the deafening then ignore.
            if(sourceMember.getUser().isBot())
                return;
            Member targetMember = event.getMember();
            guild.deafen(targetMember, false).queue();
            guild.deafen(sourceMember, true).queue();
        });
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        Guild guild = event.getGuild();
        guild.retrieveAuditLogs().type(ActionType.MEMBER_VOICE_KICK).limit(1).queue(entryLog -> {
            if(entryLog.isEmpty())
                return;
            AuditLogEntry entry = entryLog.get(0);
            Member sourceMember = guild.getMember(entry.getUser());
            // We can't kick someone who's not in a voice channel...
            if(!sourceMember.getVoiceState().inAudioChannel())
                return;
            // If the bot did the kicking then ignore. Mostly a redundant check as the bot won't be in a voice channel.
            if(sourceMember.getUser().isBot())
                return;
            guild.kickVoiceMember(sourceMember).queue();
        });
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();
        guild.retrieveAuditLogs().limit(1).queue(entryLog -> {
            if(entryLog.isEmpty())
                return;
            AuditLogEntry entry = entryLog.get(0);
            ActionType entryActionType = entry.getType();
            if(!(entryActionType == ActionType.BAN || entryActionType == ActionType.KICK))
                return;
            Member sourceMember = guild.getMember(entry.getUser());
            // We can't kick or ban the owner of a server.
            if(guild.getOwnerId().equals(sourceMember.getId()))
                return;
            if(entryActionType == ActionType.BAN) {
                guild.ban(sourceMember, 0);
            } else {
                guild.kick(sourceMember);
            }
        });
    }

}
