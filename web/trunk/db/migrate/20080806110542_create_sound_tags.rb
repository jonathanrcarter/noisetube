class CreateSoundTags < ActiveRecord::Migration
  def self.up
    create_table :sound_tags do |t|
      t.references :sound
      t.references :tag
    end
    remove_column :tags,"sound_id"
  end

  def self.down
    drop_table :sound_tags
    add_column :tags,"sound_id",:integer
  end
end
